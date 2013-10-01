var assert = require('assert'),
    fs = require('fs'),
    path = require('path'),
    async = require('async'),
    request = require('request'),
    vows = require('vows')
  ;

vows.describe("").addBatch({
  "With a valid target server": {
    "and no latency": {
      "and no headers": macros.http.assertProxied(),
      "and headers": macros.http.assertProxied({
        request: { headers: { host: 'unknown.com' } }
      }),
      "and forwarding enabled": macros.http.assertForwardProxied()
    },
    "and latency": macros.http.assertProxied({
      latency: 2000
    })
  },
  "With a no valid target server": {
    "and no latency": macros.http.assertInvalidProxy(),
    "and latency": macros.http.assertInvalidProxy({
      latency: 2000
    })
  }
}).export(module);