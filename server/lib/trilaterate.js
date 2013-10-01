

function Station(id, x, y) {
	this.id = id;
	this.x = x;
	this.y = y;
}

function ScanResult(id, station, strength) {

	this.id = id;
	this.station = station;
	this.strength = strength;

}

Trilaterate.prototype.scenResult = function(id, station, strength) {
	return new ScanResult(id, station, strength);
}

Trilaterate.prototype.station = function(id, x, y) {
	return new Station(id, x, y);
}

function Trilaterate() {

	console.log("Trilaterate constructor");
}

Trilaterate.prototype.locate = function(results) {



}



module.exports = Trilaterate;