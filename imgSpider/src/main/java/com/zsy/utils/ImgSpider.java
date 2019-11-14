package com.zsy.utils;

import java.awt.BorderLayout;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;



public class ImgSpider {
	
	private String filePath="C:\\";
	//private String filePath="E:\\test\\";
	//private  String filePath="E:/eclipseworkspace/imgSpider/img/";
	private final int threadNum=5;
	private final int imgSize=20*1024;
	private ExecutorService pool;
	private ConcurrentHashMap<String,Object> URLMap=new ConcurrentHashMap<>();
	private ConcurrentHashMap<String,Object> imgURLMap=new ConcurrentHashMap<>();
	private final Object mapValue=new Object(); 
	
	public String getHtml(String myURL) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        String html="";
        HttpGet request = new HttpGet(myURL);
        request.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.120 Safari/537.36");
        
        try {
            //3.执行get请求，相当于在输入地址栏后敲回车键
            response = httpClient.execute(request);
            
            //4.判断响应状态为200，进行处理
            if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                //5.获取响应内容
                HttpEntity httpEntity = response.getEntity();
                html = EntityUtils.toString(httpEntity, "utf-8");
            } else {
                //如果返回状态不是200，比如404（页面不存在）等，根据情况做处理，这里略
                System.out.println("返回状态不是200");
                System.out.println(EntityUtils.toString(response.getEntity(), "utf-8"));
            }
        } catch (ParseException | IOException e) {
             e.printStackTrace();
        } finally {
            //6.关闭
            HttpClientUtils.closeQuietly(response);
            HttpClientUtils.closeQuietly(httpClient);
        }
        return html;
	}
	
	public int getThreadNum() {
		return threadNum;
	}

	public void getImages(String html,String myURL) {
		Document document = Jsoup.parse(html);
		//像js一样，通过标签获取title
        //获取所有图片
        Elements imgs=document.getElementsByTag("img");
        for(Element img:imgs) {
        	String imgUrl = img.attr("src");
        	if(imgUrl.startsWith("//")) {
        		imgUrl="http:"+imgUrl;
        	}
        	else if(imgUrl.startsWith("/")){
				imgUrl=getHostName(myURL)+imgUrl;
			}
        	if(imgUrl==null||imgUrl.equals("")||imgURLMap.containsKey(imgUrl)) {
        		continue;
        	}
        	System.out.println(imgUrl);
        	if(imgUrl.startsWith("http")) {
        		downImagesByHttp(imgUrl);
        	}
        	else {
				downImagesByBase64(imgUrl);
			}
        }
	}

	public void getAllImages(String myURL) {
		System.out.println(myURL);
		String html=getHtml(myURL);
		getImages(html,myURL);
		URLMap.put(myURL,mapValue);
		String hostName=getHostName(myURL);
		LinkedList<String> curURLs=new LinkedList<>();
		Document document = Jsoup.parse(html);
		Elements links=document.getElementsByTag("a");
		for(Element link:links) {
			String nextLink=link.attr("href");
			if(nextLink.startsWith("http")||nextLink.startsWith("/")) {
				if(nextLink.startsWith("/")) {
					nextLink=hostName+nextLink;
				}
				if(!URLMap.containsKey(nextLink)) {
					curURLs.add(nextLink);
				}
			}
		}
		while(!curURLs.isEmpty()) {
			String curURL=curURLs.pollFirst();
			pool.submit(()->{
				getAllImages(curURL);
			});
		}
	}
	
	private String getHostName(String myURL) {
		return myURL.substring(0,myURL.indexOf("/",8));
	}
	
	private void downImagesByBase64(String imgUrl) {
		// TODO Auto-generated method stub
		String fileName="."+imgUrl.substring(imgUrl.indexOf('/')+1, imgUrl.indexOf(';'));
		String fileBase64=imgUrl.substring(imgUrl.indexOf(',')+1);
		File file=null;
		FileOutputStream out = null;
		try {
			file=new File(filePath+"zsy"+UUID.randomUUID().toString().substring(28)+fileName);
			out = new FileOutputStream(file);
			byte[] b=Base64.decodeBase64(fileBase64);
			out.write(b);
		} catch (Exception e) {
			 e.printStackTrace();
		}
		finally {
			try {
				out.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void downImagesByHttp(String imgUrl){
		imgURLMap.put(imgUrl, mapValue);
		String fileName = imgUrl.substring(imgUrl.lastIndexOf("."));
		HttpURLConnection connection=null;
		InputStream is = null;
		File file=null;
		FileOutputStream out = null;
		
		try {
			URL url = new URL(imgUrl);
			connection = (HttpURLConnection)url.openConnection();
			if(connection.getContentLength()>imgSize) {
				is = connection.getInputStream();
				if(fileName.matches(".+?((png)|(jpg)|(jpeg)|(gif)|(svg))$")) {
					file=new File(filePath+"zsy"+UUID.randomUUID().toString().substring(28)+fileName);
				}
				else {
					file=File.createTempFile("zsy", ".png",new File(filePath));
				}
				out = new FileOutputStream(file);
				int i = 0;
				while((i = is.read()) != -1){
					out.write(i);
				}
			}
		} catch (Exception e) {
			 e.printStackTrace();
		}
		finally {
			try {
				connection.disconnect();
				out.close();
				is.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void init() {
		JFrame uiFrame=new JFrame("世缘科技");
    	JPanel panelURL=new JPanel();
    	JTextField urlField=new JTextField(40);
    	JButton start=new JButton("开始");
    	JButton stop=new JButton("结束");
    	panelURL.add(urlField);
    	panelURL.add(start);
    	panelURL.add(stop);
    	
    	//urlField.setText("");
    	
    	start.addActionListener(startEve->{
    		pool=Executors.newFixedThreadPool(threadNum);
    		pool.submit(()->{
    			if(!urlField.getText().startsWith("http")) {
    				urlField.setText("http://"+urlField.getText());
    			}
    			getAllImages(urlField.getText());
    		});
    	});
    	
    	stop.addActionListener(stopEve->{
    		if(!pool.isShutdown()) {
    			pool.shutdownNow();
    		}
    		URLMap.clear();
    		imgURLMap.clear();
    	});
    	
    	JPanel panelFilePath=new JPanel();
    	JTextField filePathField=new JTextField(44);
    	JButton choose=new JButton("选择文件");
    	panelFilePath.add(filePathField);
    	panelFilePath.add(choose);
    	
    	filePathField.setText(filePath);
    	choose.addActionListener(chooseEve->{
    		JFileChooser fileChooser=new JFileChooser(".");
    		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    		int result=fileChooser.showDialog(uiFrame,"选择存储路径");
			if(result==JFileChooser.APPROVE_OPTION){
				filePath=fileChooser.getSelectedFile().getPath()+"\\";
				filePathField.setText(filePath);
			}
    	});
    	
    	uiFrame.add(panelURL);
    	uiFrame.add(panelFilePath,BorderLayout.SOUTH);
    	uiFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	uiFrame.pack();
    	uiFrame.setLocation(500, 200);
    	uiFrame.setVisible(true);
	}
}

