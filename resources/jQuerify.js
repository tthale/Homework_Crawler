/** dynamically load jQuery  - pulled in by JQueryLoader.java */
(function(jqueryUrl, callback) {
    if (typeof jqueryUrl != 'string') {
        jqueryUrl = 'https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js';
    }
    if (typeof jQuery == 'undefined') {
        var script = document.createElement('script');
        var head = document.getElementsByTagName('head')[0];
        var done = false;
        script.onload = script.onreadystatechange = (function() {
            if (!done && (!this.readyState || this.readyState == 'loaded' 
                    || this.readyState == 'complete')) {
                done = true;
                script.onload = script.onreadystatechange = null;
                head.removeChild(script);
                callback();
            }
        });
        script.src = jqueryUrl;
        head.appendChild(script);
    }
    else {	// I suppose here would be a place to Javasscript some indicator that jQuery didn't load
        callback();
    }
})(arguments[0], arguments[arguments.length - 1]);