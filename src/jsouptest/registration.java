package jsouptest;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class registration {

	public static void main(String[] args) {
		File input = new File("C:\\Users\\Zach\\dev\\jsouptest\\src\\registration.html");
		try{
			Document document = Jsoup.parse(input, "UTF-8");
			Elements dataRows = document.getElementsByClass("plaintable");
			
			
			Map<String, String> registrationErrors = new HashMap<String, String>();
			for(Element row : dataRows){

		            //Check if an error exists
		            if(row.toString().contains("errortext")){

		                //If so, determine what error is present
		                Elements links = document.select("a[href]");

		                //Insert list of CRNs and errors into a map
		                for(Element link : links){

		                    if(link.toString().contains("http://www.is.mcgill.ca/whelp/sis_help/rg_errors.htm")){
		                    	
		                    	System.out.println("***************************");
		                    	System.out.println(link.parent().parent().child(1));
		                    	
		                    	System.out.println("***************************");
		                    	System.out.println(link);
		                    	//System.out.println("Error: " + link.text());
		                    	
		                        //String CRN = link.parent().child(1).text();
		                        //String error = link.text();
		                        //System.out.println("CRN: " + CRN + " ERROR: " + error);
		                        //registrationErrors.put(CRN, error);
		                    }
		                }
		            }
		        }
		} catch(Exception e){
			e.printStackTrace();
			System.out.println("FAILED");
		}

	}

}
