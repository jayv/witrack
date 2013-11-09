var http = require('http');
var fs = require('fs');
var util = require('util');
var events = require('events');
var url = require('url');
var path = require('path');
var spawn = require('child_process').spawn;
var exec = require('child_process').exec;
var moment = require('moment');
var socketIoClient = require('socket.io-client');

var exit;

var scannerId = fs.readFileSync("/sys/class/net/eth0/address").toString().trim();

console.log(new Date().toISOString(), "Enable scanning: ", scannerId);

var socket = socketIoClient.connect('http://10.0.1.21:3000', {
    'max reconnection attempts': Number.MAX_VALUE
});
socket.on('connect', function(){    
    console.log(new Date().toISOString(), "Connected!");
    socket.on('disconnect', function(){
        console.log(new Date().toISOString(), "Disconnected? :-(");
    });
    socket.on('reconnect', function(){
        console.log(new Date().toISOString(), "Re-Connected!");
    });
    socket.on('reconnect_failed', function(){
        console.log(new Date().toISOString(), "Re-Connect Failed! :-(");
        exit();
    });
});

var parseFile = function(data) {

    var result = {
        stamp: moment().utc(),
        scannerId: scannerId,
        scans: []  
    };

    var lines = data.trim().split('\n');

    var stationSection = false;

    var IDX_MAC = 0
      , IDX_FIRST_TIME = 1 
      , IDX_LAST_TIME = 2
      , IDX_POWER = 3
      , IDX_PACKETS = 4;

    lines.forEach(function(line){

        // skip to station section
        var fields = line.split(',');
        if (fields && fields[IDX_MAC] == 'Station MAC') {
            stationSection = true;
            return;
        }

        if (stationSection) {

            var scan = {
                mac: fields[IDX_MAC],
                firstSeen: moment(fields[IDX_FIRST_TIME]).utc(),
                lastSeen: moment(fields[IDX_LAST_TIME]).utc(),
                power: parseInt(fields[IDX_POWER]),
                packets: parseInt(fields[IDX_PACKETS])
            };
            
            result.scans.push(scan);
        }

    });

    return result;
}

var collect = function() {

    var file = 'data'

    var stopCollecting;

    exec('airmon-ng | grep mon0', function(error){

        if (error && error.code === 1) {

            exec('airmon-ng start wlan0', function(error){

                if (error) {

                    console.log(new Date().toISOString(), "Failed to enable Monitoring Mode, quitting.");

                    process.exit();

                } else {

                    console.log(new Date().toISOString(), "Monitoring Mode enabled, start collecting...");

                    stopCollecting = startCollecting();

                }

            });

        } else {

            console.log(new Date().toISOString(), "Monitoring Mode already enabled, start collecting...");

            stopCollecting = startCollecting() 

        }

    });

    var startCollecting = function() {

        var procDump = spawn('airodump-ng', ['-o', 'csv', '-w', file, 'mon0']);

        var infoFile = file + '-01.csv'
        
        if (fs.existsSync(infoFile)) {
            fs.unlinkSync(infoFile);
        }

        var hasCalledErr = false;
        var isDead = false;
        fs.watchFile(infoFile, function(curr, prev) {
            if(isDead) {
                return;
            }
            fs.readFile(infoFile, function(err, data) {

                if(err) {
                    console.log(new Date().toISOString(), "Error:", err);
                    throw err;                
                }
                try {
                    var data = data.toString('utf8');
                    var scan = parseFile(data);
                    //console.log("New data", JSON.stringify(scan));
                    socket.emit("scan", scan);
                } catch(err) {
                    console.log(new Date().toISOString(), "Error:", err);
                    return;
                }            
                if(isDead) {
                    console.log(new Date().toISOString(), "Process isDead");
                    return;
                }

            });
        });
        procDump.stderr.setEncoding('utf8');
        procDump.stderr.on('data', function(data) {
            if(isDead) {
                return;
            }
        });
        procDump.on('exit', function(code) {
            fs.unwatchFile(infoFile)
            console.log(new Date().toISOString(), 'child process exited with code ' + code);

            console.log(new Date().toISOString(), 'attempting restart...');
            exit = collect();
        });
        return function() {
            console.log(new Date().toISOString(), "Collector exiting..." )
            fs.unwatchFile(infoFile)
            isDead = true
            procDump.kill()
        }
    };

    return function() {
        stopCollecting && stopCollecting();
    }
};

process.on('SIGINT', function() {
  console.log(new Date().toISOString(), "gracefully shutting down from SIGINT (Crtl-C)" )
  exit();
  process.exit();
});

exit = collect();

