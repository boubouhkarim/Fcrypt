package client;

import com.dropbox.core.DbxDownloader;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.*;

import java.io.*;

public class Dropbox {
	private static final String ACCESS_TOKEN = "KOLhvLnGfDAAAAAAAAAAERkLNYzoh-6MByXUGv5aS5NRylvy25-CASZqDneT4Gh8";
	
	private DbxRequestConfig config ;
	private DbxClientV2 client ;
	
	@SuppressWarnings("deprecation")
	public Dropbox() {
		this.config = new DbxRequestConfig("dropbox/cryptoappdna", "en_US");
		this.client = new DbxClientV2(config, ACCESS_TOKEN);
	}
	
	// Function for upload
	public static void UPLOAD(DbxClientV2 client, String Address,String name) throws FileNotFoundException, IOException, UploadErrorException, DbxException{
		// Upload "test.txt" to Dropbox
				try (InputStream in = new FileInputStream(Address)) {
				     FileMetadata metadata = client.files().uploadBuilder("/"+name).uploadAndFinish(in);
			        } // try upload
	}
	
	// Function for download
	public static void DOWNLOAD(DbxClientV2 client, String Address,String name) throws DownloadErrorException, DbxException, IOException{
		DbxDownloader<FileMetadata> downloader = client.files().download("/"+name);
		try {
		     FileOutputStream out = new FileOutputStream(Address+name);
		     downloader.download(out);
		     out.close();}// try 
		        
		catch (DbxException ex) {System.out.println(ex.getMessage());}
	}
	
	// Show files
	public static void Files(DbxClientV2 client) {
			try {
			System.out.println("\nList of files in DropBox:");
			
			ListFolderResult result = client.files().listFolder("");
			while (true) {
				for (Metadata metadata : result.getEntries()) {
					System.out.println(metadata.getPathLower().split("/")[1]);}
				if (!result.getHasMore()) {break;}
				result = client.files().listFolderContinue(result.getCursor());
			} // while
			
		} catch (DbxException ex1) {ex1.printStackTrace();}
	}
	
	
	public static String getAccessToken() {			return ACCESS_TOKEN;}

	public DbxRequestConfig getConfig()   {			return config;}

	public DbxClientV2 getClient() 		  {			return client;}

	public void setConfig(DbxRequestConfig config)  {			this.config = config;}

	public void setClient(DbxClientV2 client)  		{			this.client = client;}


}
