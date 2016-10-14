var getCurrentScript = function () {
  if (document.currentScript) {
    return document.currentScript.src;
  } else {
    var scripts = document.getElementsByTagName('script');
    return scripts[scripts.length-1].src;

  }
};

var getCurrentServer = function(scriptPath){
      var l = document.createElement("a");
      l.href = scriptPath;
      return l.hostname;
}
var server = getCurrentServer(getCurrentScript());
if(server!='localhost' && server!='nicokrause.com') //development/integration/production server!
        server = "webrtc.a-fk.de"; // getCurrentServer(); //change it in status.js / index.js too

console.log('server: '+getCurrentServer());
document.write("<script type='text/javascript' src='https://" + server + "/jWebrtc/bower_components/jquery/dist/jquery.min.js'></script>"); 
document.write("<script type='text/javascript' src='https://" + server + "/jWebrtc/bower_components/adapter.js/adapter.js'></script>"); 
document.write("<script type='text/javascript' src='https://" + server + "/jWebrtc/bower_components/kurento-utils/js/kurento-utils.js'></script>"); 
document.write("<script type='text/javascript' src='https://" + server + "/jWebrtc/js/status.js'></script>"); 
document.write("<link rel='stylesheet' href='https://" + server + "/jWebrtc/css/status.css'>"); 

