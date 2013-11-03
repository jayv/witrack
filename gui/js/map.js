/**
 * Represents a room that can be rendered on the map.
 *
 * @param id the unique id of the room (used to associate with areas)
 * @param x the X position of the room
 * @param y the Y position of the room
 * @param width the width of the room
 * @param height the height of the room
 * @param maxDevices the maximum number of devices that can be rendered in this room
 */
function Room(id, x, y, width, height, maxDevices) {
    var self = this;

    self.id = id;
    self.x = x;
    self.y = y;
    self.width = width;
    self.height = height;
    self.maxDevices = maxDevices;

    /**
     * Draws the room onto the given SVG group.
     *
     * @param g the SVG group
     */
    self.draw = function(g) {
        g.append('rect')
                .attr('id', self.id)
                .attr('class', 'room')
                .attr('x', self.x).attr('y', self.y)
                .attr('width', self.width).attr('height', self.height);
    };

    /**
     * Returns the viewbox of the room (think of it as the bounding box of a room). This can be used to adapt the
     * viewbox of the parent SVG to for example zoom in on one specific room.
     *
     * @returns {string} the viewbox of the room
     */
    self.getViewBox = function() {
        var padding = 500;

        return Math.min(self.x, self.x - padding) + ' ' + Math.min(self.y, self.y - padding) + ' ' +
                (self.width + padding) + ' ' + (self.height + padding);
    };

    /**
     * @returns {string} a string representation of this room
     */
    self.toString = function() {
        return '[' +
            'id=' + self.id + ', ' +
            'x=' + self.x + ', ' +
            'y=' + self.y + ', ' +
            'width=' + self.width + ', ' +
            'height=' + self.height + ', ' +
            'maxDevices= ' + self.maxDevices + ']';
    };
}

/**
 * Represents a wall that can be rendered on the map.
 *
 * @param id the unique id of the wall (mainly for debug purposes and not really used in code)
 * @param points the points that make up the line (x, y)
 */
function Wall(id, points) {
    var self = this;

    self.id = id;
    self.points = points;

    /**
     * Draws the wall onto the given SVG group.
     *
     * @param g the SVG group
     */
    self.draw = function(g) {
        g.append('polyline').attr('class', 'wall').attr('fill', 'none').attr('points', points.join(' '));
    }
}

/**
 * Represents a device that can be rendered in an area on the map.
 *
 * @param id the unique id of the device
 * @param x the X position of the device
 * @param y the Y position of the device
 */
function Device(id, x, y) {
    var self = this;

    self.id = id;
    self.x = x;
    self.y = y;
    self.originalX = x;
    self.originalY = y;
    self.radius = 50;

    /**
     * @returns {string} a string representation of this device
     */
    self.toString = function() {
        return '[id=' + self.id + ', x=' + self.x + ', y=' + self.y + ', originalX=' + self.originalX +
            ', originalY=' + self.originalY + ']'
    };
}

/**
 * A blocked area is a zone within an area in which you don't want devices to appear (e.g. a staircase). You can
 * configure an array of actions that need to be performed on devices that are located in this area (e.g. move left,
 * right, up or down).
 *
 * @param x the X where the blocked area starts
 * @param y the Y where the blocked area starts
 * @param w the width of the blocked area
 * @param h the height of the blocked area
 * @param actions the actions to perform to devices located in the blocked area
 */
function BlockedArea(x, y, w, h, actions) {
    var self = this;

    self.x = x;
    self.y = y;
    self.w = w;
    self.h = h;
    self.actions = actions;

    /**
     * @param d the device
     * @return boolean true if the given device is within the bounds of the blocked area, false otherwise
     */
    self.isInBounds = function(d) {
        return d.x > self.x && d.x < self.x + self.w && d.y > self.y && d.y < self.y + self.h;
    };

    /**
     * @param d the device
     * @return Array containing the distances per action (left, right, ...) sorted by least distance first
     */
    self.calculateDistances = function(d) {
        var distances = [];

        if (self.actions.indexOf('left') != -1) {
            distances.push({action: 'left', distance: d.x - self.x});
        }

        if (self.actions.indexOf('right') != -1) {
            distances.push({action: 'right', distance: (self.x + self.w) - d.x});
        }

        if (self.actions.indexOf('up') != -1) {
            distances.push({action: 'up', distance: d.y - self.y});
        }

        if (self.actions.indexOf('down') != -1) {
            distances.push({action: 'down', distance: (self.y + self.h) - d.y});
        }

        return distances.sort(self.sortByDistance());
    };

    /**
     * @returns {Function} that sorts by least distance first
     */
    self.sortByDistance = function() {
        return function(a, b){
            return ~~(a['distance'] > b['distance']);
        }
    };

    /**
     * Moves the given device outside of this blocked area. Moving is only done based on the allowed actions configured
     * on this blocked area (actions: left, right, up, ...). The device is moved to the closest border.
     *
     * @param d the device to move
     */
    self.moveOutsideBlockedArea = function(d) {
        var distances = self.calculateDistances(d);
        var minDistance = distances[0];

        if (minDistance.action == 'left') {
            d.x = d.cx = self.x;
        }

        if (minDistance.action == 'right') {
            d.x = d.cx = self.x + self.w;
        }

        if (minDistance.action == 'up') {
            d.y = d.cy = self.y;
        }

        if (minDistance.action == 'down') {
            d.y = d.cy = self.y + self.h;
        }
    };
}

/**
 * Represents an area in which devices can be rendered. An area comes with a D3 force object that will prevent devices
 * from overlapping. An area is always associated with a specific room. When devices are added to an area, only devices
 * that finds into the bounds of the room are accepted. Areas are rendered into a separate SVG group.
 *
 * @param svg the SVG on which the area and associated devices should be rendered
 * @param room the room associated with this area
 */
function Area(svg, room) {
    var self = this;

    self.g = svg.append('g').attr('id', 'area-for-' + room.id);
    self.room = room;
    self.devices = [];
    self.blockedAreas = [];

    /**
     * Tick function to support the D3 force object.
     *
     * @param e
     */
    self.tick = function(e) {
        self.g.selectAll('circle').data(self.devices)
            .each(self.gravity(.2 * e.alpha))
            .each(self.moveOutsideBlockedArea())
            .each(self.collide(.5))
            .attr('cx', function(d) {
                return d.x = Math.max(self.room.x + 150, Math.min((self.room.x + self.room.width) - 150, d.x));
            })
            .attr('cy', function(d) {
                return d.y = Math.max(self.room.y + 150, Math.min((self.room.y + self.room.height) - 150, d.y));
            });
    };

    /**
     * Function called by the tick to move devices that would be rendered in a blocked area outside of the blocked area
     * by moving it to the nearest border (e.g. left, right, ...)
     *
     * @returns {Function} function to move devices outside of blocked areas
     */
    self.moveOutsideBlockedArea = function() {
        return function(d) {
            for (var i = 0; i < self.blockedAreas.length; i++) {
                var zone = self.blockedAreas[i];

                if (zone.isInBounds(d)) {
                    zone.moveOutsideBlockedArea(d);
                }
            }
        }
    };

    /**
     * The D3 force object to keep devices as close as possible to their located position, prevent devices from
     * overlapping or going into blocked areas.
     */
    self.force = d3.layout.force()
            .nodes(self.devices)
            .size([self.room.width, self.room.height])
            .friction(0.5)
            .gravity(0)
            .charge(0)
            .on('tick', self.tick);

    /**
     * Adds a device to the area. Only devices that fit in the bounds of the room are accepted.
     *
     * @param device the device to add
     */
    self.addDevice = function(device) {
        if (self.isDeviceInBounds(device)) {
            if (self.devices.length >= self.room.maxDevices) {
                self.devices.shift(); // Maximum reached, remove the first device.
            }

            self.devices.push(device);
            self.renderDevices();
        }
    };

    /**
     * Updates a device within this area. If a device was already active in this area, it's position will be updated. If
     * the device was not yet active in this area but is within the bounds of this area, it is added. If this device
     * used to be in this area bounds but is no longer, it will be removed.
     *
     * @param device the device to update
     */
    self.updateDevice = function(device) {
        var index = self.findDeviceIndex(device.id);

        if (self.isDeviceInBounds(device)) {
            if (index != -1) {
                self.devices[i] = device;
            } else {
                self.addDevice(device);
            }
        } else {
            if (index != -1) {
                self.removeDevice(device.id);
            }
        }
    };

    /**
     * Remove a device from this area.
     *
     * @param id the id of the device to remove
     */
    self.removeDevice = function(id) {
        var index = self.findDeviceIndex(id);

        if (index !== -1) {
            self.devices.splice(index, 1);
            self.renderDevices();
        }
    };

    /**
     * Removes all devices from this area.
     */
    self.removeAllDevices = function() {
        self.devices = [];
        self.renderDevices();
    };

    /**
     * Removes all devices active on this area and replaces them with the given devices. This is faster than adding
     * device per device because the rendering is only called once. Only devices that fit within the bounds of this area
     * are accepted.
     *
     * @param devices the devices to render on this area.
     */
    self.setDevices = function(devices) {
        self.devices = [];

        devices.forEach(function(device) {
            if (self.isDeviceInBounds(device)) {
                self.devices.push(device);
            }
        });

        self.renderDevices();
    };

    /**
     * Find the index of a device.
     *
     * @param id the id of the device
     * @returns {number} the index of the device
     */
    self.findDeviceIndex = function(id) {
        for (var i = 0; i < self.devices.length; i++) {
            if (self.devices[i].id === id) {
                return i;
            }
        }

        return -1;
    };

    /**
     * Returns true if the device is in the bounds of this area, false otherwise.
     *
     * @param device the device
     * @returns {boolean} true if the device is in the bounds of this area, false otherwise.
     */
    self.isDeviceInBounds = function(device) {
        return device.x > self.room.x &&
                device.y > self.room.y &&
                device.x < self.room.x + self.room.width &&
                device.y < self.room.y + self.room.height
    };

    /**
     * Render the devices onto the SVG group and activates the D3 force object.
     */
    self.renderDevices = function() {
        var circles = self.g.selectAll('circle').data(self.devices);

        circles.enter().append('circle')
                .attr('cx', function(d) {return d.x;})
                .attr('cy', function(d) {return d.y;})
                .attr('r', function(d) {return d.radius - 15;}) // Minus 15 to have some padding between circles
                .attr('class', 'device')
                .style('opacity', 0).transition().duration(1000).style('opacity', 1);

        circles.exit().remove();

        self.force.start();
    };

    /**
     * @param alpha
     * @returns {Function} a function that will keep devices as close as possible to their located position
     */
    self.gravity = function(alpha) {
        return function(d) {
            d.x += (d.originalX - d.x) * alpha;
            d.y += (d.originalY - d.y) * alpha;
        };
    };

    /**
     * @param alpha
     * @returns {Function} a function that will prevent devices from overlapping each other
     */
    self.collide = function(alpha) {
        var quadtree = d3.geom.quadtree(self.devices);

        return function(d) {
            var r = d.radius * 2;
            var nx1 = d.x - r;
            var nx2 = d.x + r;
            var ny1 = d.y - r;
            var ny2 = d.y + r;

            quadtree.visit(function (quad, x1, y1, x2, y2) {
                if (quad.point && (quad.point !== d)) {
                    var x = d.x - quad.point.x;
                    var y = d.y - quad.point.y;

                    if (x == 0 && y == 0) { // If points have the exact same x and y, manually move x an y
                        x += 1;
                        y += 1;
                    }

                    var l = Math.sqrt(x * x + y * y);
                    var r = d.radius * 2;

                    if (l < r) {
                        l = (l - r) / l * alpha;
                        d.x -= x *= l;
                        d.y -= y *= l;
                        quad.point.x += x;
                        quad.point.y += y;
                    }
                }

                return x1 > nx2 || x2 < nx1 || y1 > ny2 || y2 < ny1;
            });
        };
    };

    /**
     * @returns {string} a string representation of this area
     */
    self.toString = function() {
        return 'Area[room=' + self.room + ']';
    };
}

/**
 * Represents the map on which rooms, areas and devices can be added.
 */
function Map() {
    var self = this;

    self.div = null;
    self.svg = null;

    self.rooms = [
            new Room('room-10', 0, 1200, 1700, 2950, 400),
            new Room('room-9', 1700, 600, 2000, 3550, 600),
            new Room('room-8', 3700, 0, 2700, 4150, 1000),
            new Room('room-7', 6400, 600, 2000, 3550, 600),
            new Room('room-6', 6400, 6500, 2000, 3550, 600),
            new Room('room-5', 3700, 6500, 2700, 4150, 1000),
            new Room('room-4', 1700, 6500, 2000, 3550, 600),
            new Room('room-3', 0, 6500, 1700, 2950, 400),
            new Room('foyer', 0, 4150, 7100, 2350, 1200)
    ];

    self.walls = [
            new Wall('wall-room10', [[1350, 4150], [0, 4150], [0, 1200], [1700, 1200], [1700, 4150], [1650, 4150]]),
            new Wall('wall-room9', [[3350, 4150], [1700, 4150], [1700, 600], [3700, 600], [3700, 4150], [3650, 4150]]),
            new Wall('wall-room8', [[3750, 4150], [3700, 4150], [3700, 0], [6400, 0], [6400, 4150], [4050, 4150]]),
            new Wall('wall-room7', [[6450, 4150], [6400, 4150], [6400, 600], [8400, 600], [8400, 4150], [6750, 4150]]),
            new Wall('wall-room6', [[6750, 6500], [8400, 6500], [8400, 10050], [6400, 10050], [6400, 6500], [6450, 6500]]),
            new Wall('wall-room5', [[4050, 6500], [6400, 6500], [6400, 10650], [3700, 10650], [3700, 6500], [3750, 6500]]),
            new Wall('wall-room4', [[3650, 6500], [3700, 6500], [3700, 10050], [1700, 10050], [1700, 6500], [3350, 6500]]),
            new Wall('wall-room3', [[1650, 6500], [1700, 6500], [1700, 9450], [0, 9450], [0, 6500], [1350, 6500]])
    ];

    self.areas = [];

    self.init = function() {
        self.div = d3.select('#map');
        self.svg = self.div.append('svg')
            .attr('preserveAspectRatio', 'xMidYMid meet')
            .attr('width', self.div.style('width'))
            .attr('height', self.div.style('height')) // If not set here, Safari Mobile SVG rendering is broken
            .attr('viewBox', self.getDefaultViewBox());

        self.drawRooms();
        self.drawRoomLabels();
        self.drawWalls();
        self.drawStairs();

        self.svg.selectAll('.room').on('click', function(data, index) {
            self.zoom(self.rooms[index].id);
        });

        if (window.location.search.indexOf('debug') != -1) {
            self.svg.selectAll('g').on('mousemove', function() {
                console.log(d3.mouse(this)[0] + ' ' + d3.mouse(this)[1]);
            });
        }
    };

    self.drawStairs = function() {
        var layer = self.svg.append('g').attr('id', 'stairs-layer');

        /* Balcony stairs. */
        layer.append('polygon').attr('class', 'stairs-fill').attr('points',
            '7600,4450 7850,4550 8000,4550 8250,4650 8250,6100 8000,6000 7850,6000 7600,5900');
        layer.append('polyline').attr('class', 'stairs-border').attr('fill', 'none').attr('points',
                '7600,4450 7850,4550 8000,4550 8250,4650 8250,6100 8000,6000 7850,6000 7600,5900');

        /* Steps of the balcony stairs. */
        layer.append('line').attr('class', 'steps').attr('x1', 7650).attr('y1', 4490).attr('x2', 7650).attr('y2', 5900);
        layer.append('line').attr('class', 'steps').attr('x1', 7700).attr('y1', 4510).attr('x2', 7700).attr('y2', 5920);
        layer.append('line').attr('class', 'steps').attr('x1', 7750).attr('y1', 4530).attr('x2', 7750).attr('y2', 5940);
        layer.append('line').attr('class', 'steps').attr('x1', 7800).attr('y1', 4550).attr('x2', 7800).attr('y2', 5960);
        layer.append('line').attr('class', 'steps').attr('x1', 7850).attr('y1', 4570).attr('x2', 7850).attr('y2', 5980);

        layer.append('line').attr('class', 'steps').attr('x1', 8000).attr('y1', 4570).attr('x2', 8000).attr('y2', 5980);
        layer.append('line').attr('class', 'steps').attr('x1', 8050).attr('y1', 4590).attr('x2', 8050).attr('y2', 6000);
        layer.append('line').attr('class', 'steps').attr('x1', 8100).attr('y1', 4610).attr('x2', 8100).attr('y2', 6020);
        layer.append('line').attr('class', 'steps').attr('x1', 8150).attr('y1', 4630).attr('x2', 8150).attr('y2', 6040);
        layer.append('line').attr('class', 'steps').attr('x1', 8200).attr('y1', 4650).attr('x2', 8200).attr('y2', 6060);

        /* Stairs near room 9. */
        layer.append('polygon').attr('class', 'stairs-fill').attr('points',
                '3100,4150 3100,4450 2400,4450 2700,4300 2800,4300 3100,4150');
        layer.append('polygon').attr('class', 'stairs-wall').attr('points',
                '3100,4150 2800,4300 2700,4300 2400,4450 2400,4150 3100,4150');
        layer.append('polyline').attr('class', 'stairs-border').attr('fill', 'none').attr('points',
                '3100,4150 3100,4450 2400,4450 2400,4150 3100,4150 2800,4300 2700,4300 2400,4450');
        layer.append('line').attr('class', 'steps').attr('x1', 2500).attr('y1', 4420).attr('x2', 2500).attr('y2', 4430);
        layer.append('line').attr('class', 'steps').attr('x1', 2550).attr('y1', 4395).attr('x2', 2550).attr('y2', 4430);
        layer.append('line').attr('class', 'steps').attr('x1', 2600).attr('y1', 4370).attr('x2', 2600).attr('y2', 4430);
        layer.append('line').attr('class', 'steps').attr('x1', 2650).attr('y1', 4345).attr('x2', 2650).attr('y2', 4430);
        layer.append('line').attr('class', 'steps').attr('x1', 2700).attr('y1', 4320).attr('x2', 2700).attr('y2', 4430);

        layer.append('line').attr('class', 'steps').attr('x1', 2800).attr('y1', 4320).attr('x2', 2800).attr('y2', 4430);
        layer.append('line').attr('class', 'steps').attr('x1', 2850).attr('y1', 4295).attr('x2', 2850).attr('y2', 4430);
        layer.append('line').attr('class', 'steps').attr('x1', 2900).attr('y1', 4270).attr('x2', 2900).attr('y2', 4430);
        layer.append('line').attr('class', 'steps').attr('x1', 2950).attr('y1', 4245).attr('x2', 2950).attr('y2', 4430);
        layer.append('line').attr('class', 'steps').attr('x1', 3000).attr('y1', 4220).attr('x2', 3000).attr('y2', 4430);
        layer.append('line').attr('class', 'steps').attr('x1', 3050).attr('y1', 4195).attr('x2', 3050).attr('y2', 4430);

        /* Stairs near room 4. */
        layer.append('polygon').attr('class', 'stairs-fill').attr('points',
            '3100,6200 3100,6500 2400,6500 2700,6350 2800,6350 3100,6200');
        layer.append('polygon').attr('class', 'stairs-wall').attr('points',
            '3100,6200 2800,6350 2700,6350 2400,6500 2400,6200 3100,6200');
        layer.append('polyline').attr('class', 'stairs-border').attr('fill', 'none').attr('points',
            '3100,6200 3100,6500 2400,6500 2400,6200 3100,6200 2800,6350 2700,6350 2400,6500');
        layer.append('line').attr('class', 'steps').attr('x1', 2500).attr('y1', 6470).attr('x2', 2500).attr('y2', 6480);
        layer.append('line').attr('class', 'steps').attr('x1', 2550).attr('y1', 6445).attr('x2', 2550).attr('y2', 6480);
        layer.append('line').attr('class', 'steps').attr('x1', 2600).attr('y1', 6420).attr('x2', 2600).attr('y2', 6480);
        layer.append('line').attr('class', 'steps').attr('x1', 2650).attr('y1', 6395).attr('x2', 2650).attr('y2', 6480);
        layer.append('line').attr('class', 'steps').attr('x1', 2700).attr('y1', 6370).attr('x2', 2700).attr('y2', 6480);

        layer.append('line').attr('class', 'steps').attr('x1', 2800).attr('y1', 6370).attr('x2', 2800).attr('y2', 6480);
        layer.append('line').attr('class', 'steps').attr('x1', 2850).attr('y1', 6345).attr('x2', 2850).attr('y2', 6480);
        layer.append('line').attr('class', 'steps').attr('x1', 2900).attr('y1', 6320).attr('x2', 2900).attr('y2', 6480);
        layer.append('line').attr('class', 'steps').attr('x1', 2950).attr('y1', 6295).attr('x2', 2950).attr('y2', 6480);
        layer.append('line').attr('class', 'steps').attr('x1', 3000).attr('y1', 6270).attr('x2', 3000).attr('y2', 6480);
        layer.append('line').attr('class', 'steps').attr('x1', 3050).attr('y1', 6245).attr('x2', 3050).attr('y2', 6480);

        /* Balcony at the right side of the Foyer */
        layer.append('polyline').attr('class', 'balcony').attr('points',
                '7100,4150 7100,4450 7600,4450 7600,5900 7400,5900 7400,5900 7390,5980 7380,6010 7360,6050 7340,6080 ' +
                '7280,6140 7250,6160 7210,6180 7180,6190 7100,6200 7100,6200 7100,6500');
    };

    /**
     * Returns the default viewbox of this SVG. This value is determined by the boundaries of all objects added to the
     * SVG. A small margin was added to the default viewbox to preserve some whitespace around the map.
     *
     * @returns {string} the default viewbox
     */
    self.getDefaultViewBox = function() {
        //return '0 0 8400 10650';
        return '0 0 8800 11050';
    };

    self.drawRooms = function() {
        var layer = self.svg.append('g').attr('id', 'room-layer');

        self.rooms.forEach(function(room) {
            room.draw(layer);
        });
    };

    self.drawWalls = function() {
        var layer = self.svg.append('g').attr('id', 'wall-layer');

        self.walls.forEach(function(wall) {
            wall.draw(layer);
        });
    };

    self.drawRoomLabels = function() {
        var layer = self.svg.append('g').attr('id', 'room-label-layer');

        layer.append('text').text('9').attr('class', 'room-name').attr('x', 1850).attr('y', 4000);
        layer.append('text').text('8').attr('class', 'room-name').attr('x', 5950).attr('y', 4000);
        layer.append('text').text('7').attr('class', 'room-name').attr('x', 7950).attr('y', 4000);
        layer.append('text').text('6').attr('class', 'room-name').attr('x', 7950).attr('y', 7050);
        layer.append('text').text('5').attr('class', 'room-name').attr('x', 5950).attr('y', 7050);
        layer.append('text').text('4').attr('class', 'room-name').attr('x', 1850).attr('y', 7050);
        layer.append('text').text('3').attr('class', 'room-name').attr('x', 150).attr('y', 7050);
    };

    /**
     * Zooms in on the room with the given id.
     *
     * @param roomId the id of the room
     */
    self.zoom = function(roomId) {
        var room = self.findRoomById(roomId);

        if (room != null) {
            self.svg.selectAll('.room').on('click', null);

            var viewBox = room.getViewBox();

            if (roomId === 'foyer') { // Overwrite viewbox for foyer to include the full width (including stairs).
                viewBox = room.x + ' ' + room.y + ' 8400 ' + room.height;
            }

            self.svg.transition().duration(1000).attr('viewBox', viewBox).each('end', function() {
                self.svg.selectAll('.room').on('click', function() {
                    self.zoomOut();
                });
            });
        }
    };

    /**
     * Zooms out to the default viewbox of the SVG.
     */
    self.zoomOut = function() {
        self.svg.selectAll('.room').on('click', null);
        self.svg.transition().duration(500).attr('viewBox', self.getDefaultViewBox()).each('end', function() {
            self.svg.selectAll('.room').on('click', function(data, index) {
                self.zoom(self.rooms[index].id);
            });
        });
    };

    /**
     * Finds the room with the given id.
     *
     * @param id the unique id of the room
     * @returns {*} the room with the given id
     */
    self.findRoomById = function(id) {
        for (var i = 0; i < self.rooms.length; i++) {
            if (self.rooms[i].id == id) {
                return self.rooms[i];
            }
        }

        return null;
    };

    /**
     * Adds a device to the map. This will delegate to all the active areas.
     *
     * @param device the device to add to the map
     */
    self.addDevice = function(device) {
        self.areas.forEach(function(area) {
            area.addDevice(device);
        });
    };

    /**
     * Updates a device on the map (new X, Y position). This will delegate to all the active areas.
     *
     * @param device the device to update
     */
    self.updateDevice = function(device) {
        self.areas.forEach(function(area) {
            area.updateDevice(device);
        })
    };

    /**
     * Removes a device from the map. This will delegate to all the active areas.
     *
     * @param id the unique id of the device to remove from the map
     */
    self.removeDevice = function(id) {
        self.areas.forEach(function(area) {
            area.removeDevice(id);
        });
    };

    /**
     * Removes all devices from the map. This will delegate to all the active areas.
     */
    self.removeAllDevices = function() {
        self.areas.forEach(function(area) {
            area.removeAllDevices();
        });
    };

    /**
     * Sets all devices at once. All existing devices will be removed from the map. This method should only be used if
     * the given argument contains the full list of all devices that should be rendered. This will delegate to all the
     * active areas. This is faster than adding device per device because the rendering is only invoked once.
     *
     * @param device the devices to render on the map
     */
    self.setDevices = function(devices) {
        self.areas.forEach(function(area) {
            area.setDevices(devices);
        });
    };

    self.init();
}

$(function() {
    var map = new Map();

    // Automatically zoom in on the foyer after 5 seconds.
    setTimeout(function() {
        map.zoom('foyer');
    }, 3000);

    // Show devices in ALL rooms.
    /*for (var i = 0; i < map.rooms.length; i++) {
        map.areas.push(new Area(map.svg, map.rooms[i]));
    }*/

    var foyer = new Area(map.svg, map.findRoomById('foyer'));

    // For the foyer we want to block 2 areas (the staircases) so no devices can be rendered there.
    foyer.blockedAreas.push(new BlockedArea(2250, 4150, 1000, 450, ['left', 'right', 'down']));
    foyer.blockedAreas.push(new BlockedArea(2250, 6050, 1000, 450, ['left', 'right', 'up']));

    map.areas.push(foyer);
    map.areas.push(new Area(map.svg, map.findRoomById('room-5')));

    // Code below is to generate some test devices.
    var counter = 1;

    function generateRandomDevice() {
        var id = counter++;

        // Use random locations within the entire area, not all generated points will fall into the rendered areas.
        //var x = Math.round(Math.random() * 8400);
        //var y = Math.round(Math.random() * 10650);

        // Generate random devices within the foyer.
        var x = 0 + Math.round(Math.random() * 7100); // Offset of foyer
        var y = 4150 + Math.round(Math.random() * 2350);

        // Generate random devices within room 5.
        //var x = 3700 + Math.round(Math.random() * 2700); // Offset of room 5
        //var y = 6500 + Math.round(Math.random() * 4150);

        return new Device(id, x, y);
    }

    function setRandomDevices() {
        var randomDevices = [];

        for (var i = 0; i < 200; i++) {
            randomDevices[i] = generateRandomDevice();
        }

        map.setDevices(randomDevices);
    }

    setRandomDevices();

    setInterval(setRandomDevices, 10000);

    // Test for adding and then updating a device
    /*map.addDevice(new Device(1, 100, 4250));
    setTimeout(function() {
        map.updateDevice(new Device(1, 3800, 6600));
    }, 2000);*/

    /*setInterval(function() {
        map.addDevice(generateRandomDevice());
    }, 100);*/

    /*setTimeout(function() {
        map.removeDevice(1);
    }, 5000);*/

    /*setTimeout(function() {
        map.updateDevice(new Device(1, 50, 50));
    }, 5000);*/

    /* setTimeout(function() {
        map.setDevices([new Device(1, 300, 300)]);
    }, 5000);*/

    /*setInterval(function() {
        map.removeAllDevices();
    }, 60000);*/
});