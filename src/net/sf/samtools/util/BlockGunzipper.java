/*
 * The MIT License
 *
 * Copyright (c) 2009 The Broad Institute
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package net.sf.samtools.util;

import net.sf.samtools.SAMFormatException;

import java.util.zip.Inflater;
import java.util.zip.CRC32;
import java.util.zip.DataFormatException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import org.apache.log4j.Logger;

/**
 * Alternative to GZIPInputStream, for decompressing GZIP blocks that are already loaded into a byte[].
 * The main advantage is that this object can be used over and over again to decompress many blocks,
 * whereas a new GZIPInputStream and ByteArrayInputStream would otherwise need to be created for each
 * block to be decompressed.
 *
 * This code requires that the GZIP header conform to the GZIP blocks written to BAM files, with
 * a specific subfield and no other optional stuff.
 *
 * @author alecw@broadinstitute.org
 */
public class BlockGunzipper {
    private final Inflater inflater = new Inflater(true); // GZIP mode
    private final CRC32 crc32 = new CRC32();
    private boolean checkCrcs = false;
    private final Logger log = Logger.getLogger(BlockGunzipper.class);
    
    /** Allows the caller to decide whether or not to check CRCs on when uncompressing blocks. */
    public void setCheckCrcs(final boolean check) {
        this.checkCrcs = check;
    }

    private void p(String s) {
        System.out.println("BlockGunzipper: "+s);
         log.fatal(s);
    }
    /**
     * Decompress GZIP-compressed data
     * @param uncompressedBlock must be big enough to hold decompressed output.
     * @param compressedBlock compressed data starting at offset 0
     * @param compressedLength size of compressed data, possibly less than the size of the buffer.
     */
    void unzipBlock(byte[] uncompressedBlock, byte[] compressedBlock, int compressedLength) {
        try {
            ByteBuffer byteBuffer = ByteBuffer.wrap(compressedBlock, 0, compressedLength);
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

            // Validate GZIP header
            
            if (byteBuffer.get() != BlockCompressedStreamConstants.GZIP_ID1 ||
                    byteBuffer.get() != (byte)BlockCompressedStreamConstants.GZIP_ID2 ||
                    byteBuffer.get() != BlockCompressedStreamConstants.GZIP_CM_DEFLATE ||
                    byteBuffer.get() != BlockCompressedStreamConstants.GZIP_FLG
                    ) {
               
                String tmp = new String(byteBuffer.array());
                if (tmp.length()> 10000) tmp = tmp.substring(0, 10000);
                p("Got ByteBuffer: "+tmp);
               // p("Got compressedBlock: "+new String(compressedBlock));
                throw new SAMFormatException("Invalid GZIP header - or possibly missing /auth/ for Ion Torrent URL");
            }
            // Skip MTIME, XFL, OS fields
            byteBuffer.position(byteBuffer.position() + 6);
            if (byteBuffer.getShort() != BlockCompressedStreamConstants.GZIP_XLEN) {
                throw new SAMFormatException("Invalid GZIP header");
            }
            // Skip blocksize subfield intro
            byteBuffer.position(byteBuffer.position() + 4);
            // Read ushort
            final int totalBlockSize = (byteBuffer.getShort() & 0xffff) + 1;
            if (totalBlockSize != compressedLength) {
                throw new SAMFormatException("GZIP blocksize disagreement");
            }

            // Read expected size and CRD from end of GZIP block
            final int deflatedSize = compressedLength - BlockCompressedStreamConstants.BLOCK_HEADER_LENGTH - BlockCompressedStreamConstants.BLOCK_FOOTER_LENGTH;
            byteBuffer.position(byteBuffer.position() + deflatedSize);
            int expectedCrc = byteBuffer.getInt();
            int uncompressedSize = byteBuffer.getInt();
            inflater.reset();

            // Decompress
            inflater.setInput(compressedBlock, BlockCompressedStreamConstants.BLOCK_HEADER_LENGTH, deflatedSize);
            final int inflatedBytes = inflater.inflate(uncompressedBlock, 0, uncompressedSize);
            if (inflatedBytes != uncompressedSize) {
                throw new SAMFormatException("Did not inflate expected amount");
            }

            // Validate CRC if so desired
            if (this.checkCrcs) {
                crc32.reset();
                crc32.update(uncompressedBlock, 0, uncompressedSize);
                final long crc = crc32.getValue();
                if ((int)crc != expectedCrc) {
                    throw new SAMFormatException("CRC mismatch");
                }
            }
        } catch (DataFormatException e)
        {
            throw new RuntimeException(e);
        }
    }
}
