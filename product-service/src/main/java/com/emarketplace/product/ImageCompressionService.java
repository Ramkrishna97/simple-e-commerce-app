package com.emarketplace.product;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;

final class ImageCompressionService {
    static final int MAX_BYTES = 50 * 1024;
    private static final int MAX_DIMENSION = 1200;
    private ImageCompressionService() { }
    static byte[] compress(byte[] source) throws IOException {
        BufferedImage image=readDownsampled(source);if(image==null)throw new IllegalArgumentException("The uploaded file is not a supported image");image=scale(image,MAX_DIMENSION);
        for(int pass=0;pass<5;pass++){for(float quality:new float[]{.82f,.68f,.54f,.42f,.32f,.24f,.16f,.10f}){byte[] encoded=jpeg(image,quality);if(encoded.length<=MAX_BYTES)return encoded;}image=scale(image,Math.max(240,(int)(Math.max(image.getWidth(),image.getHeight())*.72)));}
        byte[] encoded=jpeg(image,.06f);if(encoded.length>MAX_BYTES)throw new IllegalArgumentException("Image could not be reduced below 50 KB");return encoded;
    }
    private static BufferedImage readDownsampled(byte[] source)throws IOException{try(ImageInputStream input=ImageIO.createImageInputStream(new ByteArrayInputStream(source))){Iterator<ImageReader> readers=ImageIO.getImageReaders(input);if(!readers.hasNext())return null;ImageReader reader=readers.next();try{reader.setInput(input,true,true);int width=reader.getWidth(0),height=reader.getHeight(0);int sample=Math.max(1,(int)Math.ceil(Math.max(width,height)/1600.0));ImageReadParam param=reader.getDefaultReadParam();param.setSourceSubsampling(sample,sample,0,0);return reader.read(0,param);}finally{reader.dispose();}}}
    private static BufferedImage scale(BufferedImage source,int maximum){double factor=Math.min(1d,maximum/(double)Math.max(source.getWidth(),source.getHeight()));int width=Math.max(1,(int)Math.round(source.getWidth()*factor)),height=Math.max(1,(int)Math.round(source.getHeight()*factor));BufferedImage target=new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);Graphics2D graphics=target.createGraphics();graphics.setColor(Color.WHITE);graphics.fillRect(0,0,width,height);graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);graphics.drawImage(source,0,0,width,height,null);graphics.dispose();return target;}
    private static byte[] jpeg(BufferedImage image,float quality)throws IOException{ByteArrayOutputStream output=new ByteArrayOutputStream(MAX_BYTES);ImageWriter writer=ImageIO.getImageWritersByFormatName("jpeg").next();try(var stream=ImageIO.createImageOutputStream(output)){writer.setOutput(stream);ImageWriteParam param=writer.getDefaultWriteParam();param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);param.setCompressionQuality(quality);writer.write(null,new IIOImage(image,null,null),param);}finally{writer.dispose();}return output.toByteArray();}
}
