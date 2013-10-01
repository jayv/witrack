

function Visualizer() {

	var self = this;

	self.graph = {
		name: "All Zones",
		children: []
	}

	var colorScale =  d3.scale.category20().range(); // Currently limited to 20 colors (== devices)

	self.devices = {};

	self.powerToPercentage = function(power) {
		var min = -82;
		var max = -18;
		if (power < min) {
			return 0;
		} else if (power > max) { 
			return 100;
		}
		return Math.floor(((power - min) / (max - min)) * 100);
	}

	self.update = function(data) {

		var zone;
		self.graph.children.forEach(function(child){
			if (child.name == data.scannerId) {
				zone = child;
			}
		});
		if (!zone) {
			zone = { name: data.scannerId, children: [] };
			self.graph.children.push(zone);
		}

		var updatedDevices = [];

		data.scans.forEach(function(scan) {

			updatedDevices.push(scan.mac);

			var color = self.devices[scan.mac] = self.devices[scan.mac] || colorScale[Object.keys(self.devices).length];

			var device;

			zone.children.forEach(function(child){
				if (child.name == scan.mac) { // Update
					device = child;
					device.size = self.powerToPercentage(scan.power);
					device.power = scan.power;
				}
			});

			if (!device) {
				device = {
					name: scan.mac,
					color: color,
					size: self.powerToPercentage(scan.power),
					power: scan.power
				}
				zone.children.push(device);
			}

		});

		zone.children.forEach(function(device, idx, array){
			if (updatedDevices.indexOf(device.name) == -1) {
				array.splice(idx, 1);
			}
		});

		console.log("Graph", self.graph);

		try {
			self.draw();
		} catch (e) {
			throw e;
		}

	};

	self.draw = function() {

		d3.selectAll("g.node").remove();

		var node = g.data([self.graph]).selectAll("g.node").data(pack.nodes);



		var circle = node.enter().append("g")
					.attr("class", function(d) { return d.children ? "node" : "leaf node"; })
					.attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; });


			circle.append("circle")
				.attr("r", function(d) { return d.r; })
				.attr("style", function(d) { return d.color ? "fill:"+d.color+";" : ""})
				.attr("alt", function(d) { return d.name; });
				

			circle.filter(function(d) { return !d.children; })
				.append("text")
					.attr("dy", ".3em")
					.style("text-anchor", "middle")
					.text(function(d) { return d.name; });		

			node.exit().remove();

	}

	var diameter = 960;

	var pack = d3.layout.pack()
		.size([diameter - 4, diameter - 4])
		.value(function(d) { return d.size; });

	var svg = d3.select("body")
		.append("svg")
			.attr("width", diameter)
			.attr("height", diameter)
			.attr("class", "pack");

	var g =	svg.append("g")
					.attr("transform", "translate(2,2)");

	// d3.select(window.self.frameElement).style("height", diameter + "px");

	return {
		update: this.update
	}
}

