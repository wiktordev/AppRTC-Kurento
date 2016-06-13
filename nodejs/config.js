var config = {}

config.port = '7080';
config.as_uri = "http://10.0.1.68:7080/";
config.ws_uri = "ws://192.168.99.100:8888/kurento";
config.outputBitrate = 3000000;
//the url of this server, this is where appRTC connects to
config.appRTCUrl = '10.0.1.68:7080';
//leave uris empty to not use turn
config.turn = {
	"username": "akashionata",
	"password": "silkroad2015",
	"uris": [
		"turn:5.9.154.226:3478",
		"turn:5.9.154.226:3478?transport=udp",
		"turn:5.9.154.226:3478?transport=tcp"
	]
};

module.exports = config;
