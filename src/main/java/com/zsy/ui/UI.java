package com.zsy.ui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

import com.zsy.utils.ImgSpider;

public class UI {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//getAllImages("https://www.nvshens.net/g/31421/");
    	new ImgSpider().init();
		/*String urlString="https://img.onvshen.com:85/article/11103/01.jpg";
		try {
			URL url=new URL(urlString);
			HttpURLConnection connection = (HttpURLConnection)url.openConnection();
			InputStream is = connection.getInputStream();
			File file = new File("E:/eclipseworkspace/imgSpider/img/zsy.jpg");
			FileOutputStream out = new FileOutputStream(file);
			int i = 0;
			while((i = is.read()) != -1){
				out.write(i);
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}

}
