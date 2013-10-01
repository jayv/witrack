var express = require('express')
  , app = express()
  , http = require('http')
  , server = http.createServer(app)
  , io = require('socket.io').listen(server);

app.get('/', function(req, res){
  res.sendfile(__dirname + '/static/index.html');
});

app.use('/static', express.static(__dirname + '/static'));

var sockets = [];

io.sockets.on('connection', function (socket) {

  socket.on('scan', function (data) {
  		io.sockets.emit('news', data);
  });

});

server.listen(3000);
console.log('Listening on port 3000');