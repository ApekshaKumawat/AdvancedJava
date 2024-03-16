import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MergeXmlFile {

    private static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy");
    private static final String LICENSE_HEADER_ROW =
   		 "nipr,License ID,Jurisdiction,Resident,License Class,License Effective Date,License Expiry Date,License Status,License Line,License Line Effective Date,License Line Expiry Date,License Line Status";


    public static void main(String[] args) throws Exception{


        try{
        File file1 = new File("License1.xml");
        File file2 = new File("License2.xml");

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();

        
        Document doc1 = db.parse(file1);
        Document doc2 = db.parse(file2);

        Map<String, List<Element>> mergedLicenseFile = mergeLicense(doc1,doc2);


        listToFile("ValidLicense.xml",mergedLicenseFile.get("valid"));

        listToFile("InvalidLicense.xml",mergedLicenseFile.get("invalid"));
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private static Map<String, List<Element>> mergeLicense(Document doc1, Document doc2) throws ParseException {
        
        Map<String, List<Element>> mergedLicense = new HashMap<>();
        mergedLicense.put("valid",new ArrayList<>());
        mergedLicense.put("invalid", new ArrayList<>());

        NodeList nodeList1 = doc1.getElementsByTagName("License");
        NodeList nodeList2 = doc2.getElementsByTagName("License");


        for(int i=0; i< nodeList1.getLength(); i++){
            Element license = (Element) nodeList1.item(i);
            if(isValid(license))
                mergedLicense.get("valid").add(license);
            else    
                mergedLicense.get("invalid").add(license);
        }

        for(int i=0; i< nodeList2.getLength(); i++){
            Element license = (Element) nodeList2.item(i);
            if(isValid(license))
                mergedLicense.get("valid").add(license);
            else    
                mergedLicense.get("invalid").add(license);
        }

        return mergedLicense;
        
    }

    private static boolean isValid(Element license) throws ParseException {

        Date expireDate = DATE_FORMAT.parse(license.getAttribute("License_Expiration_Date"));
        Date todayDate = new Date();
        return expireDate.after(todayDate);

    }
    
    private static void listToFile(String filename, List<Element> licenses) throws Exception{
        try(PrintWriter writer = new PrintWriter(new FileWriter(filename))){
            writer.println(LICENSE_HEADER_ROW);
            for(Element license : licenses){
                writer.println(formatLicense(license));
            }
        }
    }

    private static String formatLicense(Element license) {

        
        String niprNumber =getAttributeValue((Element) license.getParentNode(), "NIPR_Number");
        String licenseNumber =getAttributeValue( license,"License_Number");
        String stateCode =getAttributeValue( license,"Status_Code");
        String residentIndicator =getAttributeValue( license,"Resident_Indicator");
        String licenseClass =getAttributeValue( license,"License_Class");
        String licenseIssueDate =getAttributeValue( license,"License_Issue_Date");
        String licenseExpirationDate =getAttributeValue( license,"License_Expiration_Date");
        String licenseStatus =getAttributeValue( license,"License_Status");
        String licenseLine =getAttributeValue( license,"License_Line");
        String licenseLineEffectiveDate =getAttributeValue( license,"License_Line_Effective_Date");
        String licenseLineExpiryDate =getAttributeValue( license,"License_Line_Expiry_Date");
        String licenseLineStatus =getAttributeValue( license,"License_Line_Status");
           

        return String.format("%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s",
        niprNumber, licenseNumber, stateCode, residentIndicator, licenseClass, licenseIssueDate, licenseExpirationDate, 
        licenseStatus, licenseLine, licenseLineEffectiveDate, licenseLineExpiryDate, licenseLineStatus);
    }

    private static String getAttributeValue(Element element, String attributeName) {
       if(element.hasAttribute(attributeName))
            return element.getAttribute(attributeName);
        else    
            return "";
    }

}
