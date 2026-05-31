package com.perpetuitylab.livenews.web

object M3u8InjectedScript {
  const val MESSAGE_TYPE = "M3U8_FOUND"

  fun build(bridgeName: String = M3u8JavascriptBridge.BRIDGE_NAME): String =
    """
      (function() {
        if (window.__liveNewsM3u8CaptureInstalled) {
          return true;
        }
        window.__liveNewsM3u8CaptureInstalled = true;

        function postCandidate(url) {
          try {
            if (typeof url !== 'string') return;
            if (url.indexOf('.m3u8') === -1 && url.indexOf('.M3U8') === -1) return;

            var bridge = window['$bridgeName'];
            if (!bridge || typeof bridge.postMessage !== 'function') return;

            bridge.postMessage(JSON.stringify({
              type: '${MESSAGE_TYPE}',
              url: url
            }));
          } catch (e) {}
        }

        function scanText(text) {
          try {
            if (typeof text !== 'string') return;
            var normalized = text.replace(/\\\//g, '/');
            var matches = normalized.match(/https?:\/\/[^\s"'<>\\]+?\.m3u8(?:\?[^\s"'<>\\]*)?/gi) || [];
            for (var i = 0; i < matches.length; i++) {
              postCandidate(matches[i]);
            }
          } catch (e) {}
        }

        function scanDocument() {
          try {
            if (document.documentElement) {
              scanText(document.documentElement.innerHTML);
            }
          } catch (e) {}
        }

        function scanPerformanceEntries() {
          try {
            if (!window.performance || typeof window.performance.getEntriesByType !== 'function') return;
            var entries = window.performance.getEntriesByType('resource') || [];
            for (var i = 0; i < entries.length; i++) {
              postCandidate(entries[i].name);
            }
          } catch (e) {}
        }

        if (typeof window.fetch === 'function') {
          var originalFetch = window.fetch;
          window.fetch = function() {
            var args = Array.prototype.slice.call(arguments);
            var request = args[0];
            try {
              if (typeof request === 'string') {
                postCandidate(request);
              } else if (request && typeof request.url === 'string') {
                postCandidate(request.url);
              }
            } catch (e) {}

            return originalFetch.apply(this, args).then(function(response) {
              try {
                if (response && typeof response.url === 'string') {
                  postCandidate(response.url);
                }
              } catch (e) {}
              return response;
            });
          };
        }

        if (window.XMLHttpRequest && window.XMLHttpRequest.prototype) {
          var originalOpen = window.XMLHttpRequest.prototype.open;
          window.XMLHttpRequest.prototype.open = function(method, url) {
            try {
              if (typeof url === 'string') {
                postCandidate(url);
              }
            } catch (e) {}
            return originalOpen.apply(this, arguments);
          };
        }

        try {
          var observer = new MutationObserver(function() {
            scanDocument();
            scanPerformanceEntries();
          });
          observer.observe(document.documentElement || document, {
            childList: true,
            subtree: true,
            attributes: true
          });
        } catch (e) {}

        scanDocument();
        scanPerformanceEntries();
        window.setInterval(function() {
          scanDocument();
          scanPerformanceEntries();
        }, 750);

        true;
      })();
    """
      .trimIndent()
}
