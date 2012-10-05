This folder contains the standalone verson of IonTorrents IGV and also a Servlet to launch IGV via URL:

IgvServlet.war
===============
Tomcat app that can be used to launch igv via url and Java Web Start. 
To deploy, either copy the .war into the webapps folder, or use the tomcat admin interface to deploy it.

Once deployed, use the url:
http://myserver.com:8080/IgvServlet/igv?<parameters>

<parameters> are any parameters as used by the Broad version:

- genome: which genome to use. Example &genome=hg19
- sessionURL: location of the session file, such as an xml file or a php script.
  Example: &sessionURL=http://ionwest.itw/output3/IonWest/Auto_B17-80--R154742-B17_94118_lot2061411-EKL_18042_27586/plugin_out/variantCaller_trunk_r41451_out/igv.php3
- locus: what locus to load. Example: &locus=systematic_genome:275737-275739
- file: what file to load. Example: &file=http://www.broadinstitute.org/igvdata/encode/hg19/broadEncode/wgEncodeBroadHistoneGm12878ControlStdSig.bigWig

This servlet accepts a few additional parameters:
- maxheap: max amount of memory that IGV can use, example: &maxheap=900m
- minheap: min amount of memory that IGV will use, example: $minheap=500m
- server: which torrent server links should point back to, example &server=mytorrentserver.com

Standalone version:
==================
Save igv.jar in a folder. Open a command line, go to that folder, and type:

java -Xmx900m -jar igv.jar

(substitute 900M to the amount of memory you can/want to give IGV)





