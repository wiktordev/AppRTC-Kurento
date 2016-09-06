

echo "connecting to production..."
echo "stopping production tomcat..."
ssh -t krausni@192.168.43.6 "sudo service tomcat8 stop"
echo "copying ./jWebrtc/target/*.war  to production"
scp ./jWebrtc/target/*.war krausni@192.168.43.6:/tmp/
ssh -t krausni@192.168.43.6 "sudo rm -rf /var/tomcat8/webapps/jWebrtc*" 
ssh -t krausni@192.168.43.6 "sudo mv /tmp/jWeb*war  /var/tomcat8/webapps/jWebrtc.war"  
echo "start production tomcat8  "
ssh -t krausni@192.168.43.6 "sudo service tomcat8 start"
echo "visit http://webrtc.a-fk.de/jWebrtc"
