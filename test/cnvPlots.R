#Copyright (C) 2011 Ion Torrent Systems, Inc. All Rights Reserved
args <- commandArgs(trailingOnly=TRUE)
contigsListFile <- args[1]
cnSegmentsFile <- args[2]
allTilesFile <- args[3]
outputDir <- args[4]
wholeGenomeCNVPlotFile <- paste(outputDir,"/wholeGenomePloidy.png", sep="")

# read in the ListCbs.out file
# all sequence coordinates are 0-based in the cbs data.frame and ends are exclusive => the interval begin-end is half-open [begin,end)
cbs <- read.table(contigsListFile, header=T, stringsAsFactor=F)
cbs$Circular <- NULL
cbs$Md5 <- NULL
names(cbs) <- c("chrId", "chrName", "length")
cbs <- cbs[order(cbs$chrId),]
cbs$chrBegin <- 0
cbs$chrEnd <- cbs$length
cbs$chrEndCoord <- cumsum(as.numeric(cbs$chrEnd))
cbs$chrBeginCoord <- c(0, cbs$chrEndCoord[1:(nrow(cbs)-1)])
cbs <- cbs[cbs$chrName != "chrM",]

# read in the allTiles.tsv file
allTiles <- read.table(allTilesFile, header=T, stringsAsFactors=F)
allTiles <- merge(allTiles,cbs,by="chrName")
allTiles$plotCoord <- (allTiles$begin+(allTiles$end-1))/2 + allTiles$chrBeginCoord + 1 #plotting coordintate 1-based
allTiles <- allTiles[allTiles$chrName != "chrM",]

# plot it
png(wholeGenomeCNVPlotFile,width=10.25,height=6.25,units="in",res=100)

# plot it
plot(0,0,type="n",xlim=c(0,max(cbs$chrEndCoord)),ylim=c(0,10),
     xaxt='n',xlab="",ylab="normalized sample coverage",xaxs="i")
axis(1,at=(cbs$chrBeginCoord+cbs$chrEndCoord)/2,labels=cbs$chrName,las=2,tick=F)
for (hc in 0:10)
{
    abline(h=hc,lty="dashed", col="grey")
}
abline(v=c(0,cbs$chrEndCoord), col="grey") 

points(allTiles$plotCoord,allTiles$sampleNormCoverage/allTiles$controlNormCoveragePloidy1, pch=".", cex=2)

cn <- read.table(cnSegmentsFile, header=T, stringsAsFactors=F)
cn <- merge(cn,cbs,by="chrName")
cn <- cn[order(cn$begin+cn$chrBeginCoord),]
x <- t(matrix(c(cn$begin+cn$chrBeginCoord,cn$end+cn$chrBeginCoord),byrow=F,nrow=nrow(cn)))
x <- data.frame(x=as.vector(x),y=rep(cn$ploidy,each=2))
lines(x$x,x$y,col="red",lwd=2)

dev.off()

for (chrNm in unique(allTiles$chrName))
{
    # plot ratio
    tilesChr <- allTiles[allTiles$chrName == chrNm,]
    tilesChr$plotCoord <- (tilesChr$begin+(tilesChr$end-1))/2 + 1 #plotting coordintate 1-based
    chrPlotName <- paste(outputDir,chrNm, sep="/")
    chrPlotName <- paste(chrPlotName, ".png", sep="")
    png(chrPlotName, width=10.0,height=6.25,units="in",res=100)
    plot(0,0,type="n",xlim=c(0,cbs$chrEnd[cbs$chrName == chrNm]),ylim=c(0,10),
         xlab="position",ylab="normalized sample coverage",main=chrNm,cex=0.6)
    for (hc in 0:10)
    {
        abline(h=hc,lty="dashed", col="grey")
    }
    points(tilesChr$plotCoord,tilesChr$sampleNormCoverage/tilesChr$controlNormCoveragePloidy1,cex=0.6)
    # plot plidies
    cnChr <- cn[cn$chrName == chrNm,]
    x <- t(matrix(c(cnChr$begin,cnChr$end),byrow=F,nrow=nrow(cnChr)))
    x <- data.frame(x=as.vector(x),y=rep(cnChr$ploidy,each=2))
    lines(x$x,x$y,col="red",lwd=2)
    dev.off()
}

for (chrNm in unique(allTiles$chrName))
{
    # plot ratio
    tilesChr <- allTiles[allTiles$chrName == chrNm,]
    tilesChr$plotCoord <- (tilesChr$begin+(tilesChr$end-1))/2 + 1 #plotting coordintate 1-based
    chrPlotName <- paste(outputDir,chrNm, sep="/")
    chrPlotName <- paste(chrPlotName, "_coverage.png", sep="")
    png(chrPlotName, width=10.0,height=6.25,units="in",res=100)
    plot(0,0,type="n",xlim=c(0,cbs$chrEnd[cbs$chrName == chrNm]),ylim=c(0,10),
         xlab="position",ylab="normalized sample coverage",main=chrNm,cex=0.6)
    for (hc in 0:10)
    {
        abline(h=hc,lty="dashed", col="grey")
    }
    points(tilesChr$plotCoord,tilesChr$controlNormCoveragePloidy1,cex=0.6, col="blue")
    points(tilesChr$plotCoord,tilesChr$sampleNormCoverage,cex=0.6, col="red")
    dev.off()
}
