var mapApp = angular.module('mapApp', []);

mapApp.controller('MapCtrl', function MapCtrl($scope) {
    $scope.scale = d3.scale.linear();

    $scope.path = d3.geo.path().projection(d3.geo.transform({
        point: function (x, y) {
            var scaledX = $scope.scale(x);
            var scaledY = $scope.scale(y);

            this.stream.point(scaledX, scaledY);
        }
    }));

    $scope.svg = null;

    /*$scope.devices = [
        {id: 1, x: 1000, y: 4250, radius: 50},
        {id: 2, x: 2000, y: 4500, radius: 50},
        {id: 3, x: 3000, y: 5000, radius: 50},
        {id: 4, x: 5000, y: 6000, radius: 50},
        {id: 5, x: 7000, y: 6400, radius: 50}
    ];*/

    $scope.initMap = function () {
        d3.json('geojson/upper-floor.geojson', function(geojson) {
            // Calculate the boundaries of the features in the geojson file.
            var bounds = $scope.path.bounds(geojson);

            var minX = bounds[0][0];
            var minY = bounds[0][1];
            var maxX = bounds[1][0];
            var maxY = bounds[1][1];

            var width = maxX;
            var height = maxY / maxX * width;
            var margin = 50;

            // Define the scale based on the minimum and maxium value.
            $scope.scale.domain([minX, maxX]).range([margin, width - (margin * 2)]);

//            $scope.svg = d3.select('#map').append('svg:svg').attr('width', width).attr('height', height);
            
            $scope.svg = d3.select('#map-svg').attr('viewBox', '0 0 ' + maxX + ' ' + maxY);

            $scope.svg.append('svg:g').selectAll("path")
                .data(geojson.features)
                .enter().append("svg:path")
                .attr("d", $scope.path)
                .attr("class", function(feature) {
                    if (feature.properties) {
                        var classes = "";

                        if (feature.properties.disabled) {
                            classes += " disabled";
                        }

                        if (feature.properties.type) {
                            classes += " " + feature.properties.type;
                        }

                        return classes;
                    }

                    return null;
                });

            var radius = 5;

            function tick(e) {
                var q = d3.geom.quadtree($scope.devices),
                    i = 0,
                    n = $scope.devices.length;

                while (++i < n) {
                    q.visit(collide($scope.devices[i]));
                }

                $scope.svg.selectAll("circle")
                    .attr("cx", function(d) { return d.x = Math.max(radius, Math.min(maxX - radius, d.x)); })
                    .attr("cy", function(d) { return d.y = Math.max(radius, Math.min(maxY - radius, d.y)); });
            }

            function collide(node) {
                var r = node.radius + 16,
                    nx1 = node.x - r,
                    nx2 = node.x + r,
                    ny1 = node.y - r,
                    ny2 = node.y + r;

                return function(quad, x1, y1, x2, y2) {
                    if (quad.point && (quad.point !== node)) {
                        var x = node.x - quad.point.x,
                            y = node.y - quad.point.y,
                            l = Math.sqrt(x * x + y * y),
                            r = node.radius + quad.point.radius;
                        if (l < r) {
                            l = (l - r) / l * .5;
                            node.x -= x *= l;
                            node.y -= y *= l;
                            quad.point.x += x;
                            quad.point.y += y;
                        }
                    }

                    return x1 > nx2 || x2 < nx1 || y1 > ny2 || y2 < ny1;
                };
            }

            /*
            var force = d3.layout.force()
                .nodes($scope.devices)
                .size([maxX, maxY])
                .gravity(0)
                .charge(5)
                .friction(0.1)
                .on("tick", tick)
                .start();

            var circle = $scope.svg.selectAll("circle")
                .data($scope.devices)
                .enter().append("circle")
                .attr("r", function(d) {return d.radius - 10; })
                .style("fill", 'red');
            */
        });
    };
});


