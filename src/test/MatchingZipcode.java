package test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MatchingZipcode {
	static Map<String,String> rawPostal = new TreeMap<String,String>(); 
	static Map<String,String> rawProvince = new TreeMap<String,String>(); 
	static Map<String,String> rawDistrict= new TreeMap<String,String>(); 
	static Map<String,String> rawSubDistrict= new TreeMap<String,String>();
	static Map<String,String> subDistrictWithZipCode= new TreeMap<String,String>();
	public static void main(String[] args) throws Exception {
		
		_1_readRawZipcode();
		_2_readProvince();
		_3_readDistrict();
		_4_readSubDistrict();
		_5_buildOutput();
		_6_printOutput();
		System.out.println("done !!");
	}
	
	private static void _6_printOutput() throws Exception {
		PrintWriter pwProvince = null;
		PrintWriter pwDistrict = null;
		PrintWriter pwSubdistrict = null;
		
		try{
			pwProvince = new PrintWriter(new OutputStreamWriter(new FileOutputStream("./output/province.txt"), "TIS-620"));
			pwDistrict = new PrintWriter(new OutputStreamWriter(new FileOutputStream("./output/district.txt"), "TIS-620"));
			pwSubdistrict = new PrintWriter(new OutputStreamWriter(new FileOutputStream("./output/subdistrict.txt"), "TIS-620"));

			for(Entry<String, String> e : rawProvince.entrySet()){
				pwProvince.println(e.getKey() +"|" + e.getValue());
			}
			for(Entry<String, String> e : rawDistrict.entrySet()){
				pwDistrict.println(e.getKey() +"|" + e.getValue());
			}
			for(Entry<String, String> e : rawSubDistrict.entrySet()){
				String zipCode = subDistrictWithZipCode.get(e.getKey());
				if(zipCode == null || !zipCode.matches("^[0-9]{5}.*$")) throw new RuntimeException("invalid zipcode ["+zipCode+"]subdistrict ["+e.getKey() + "|" + e.getValue()+"]");
				pwSubdistrict.println(e.getKey() +"|" + e.getValue()+ "|" + zipCode);
				
			}
			
		}finally{
			try{pwProvince.close();}catch(Exception e){}
			try{pwDistrict.close();}catch(Exception e){}
			try{pwSubdistrict.close();}catch(Exception e){}
		}
		
	}

	private static void _5_buildOutput() {
		System.out.println(rawSubDistrict.size());
		for(Entry<String, String> e : rawSubDistrict.entrySet()){

			String provinceCode = e.getKey().substring(0,2);
			String districtcode = e.getKey().substring(0,4);
			String province = rawProvince.get(provinceCode);
			String district = rawDistrict.get(districtcode);
			String findKey = createKey(provinceCode, province, district,e.getValue());
			
			String zipCode = rawPostal.get(findKey);
			if(zipCode == null)
//				throw new RuntimeException("cannot find zipcode from findKey " + e.getKey() + "|" + e.getValue() + "["+districtcode+"]["+findKey+"]");
				System.out.println("cannot find zipcode from findKey " + e.getKey() + "|" + e.getValue() + "["+districtcode+"]["+findKey+"]");
			
			subDistrictWithZipCode.put(e.getKey(),zipCode);
		}
		
	}
	public static void _4_readSubDistrict() throws IOException {
		BufferedReader br = null;
		try{
			File raw = new File("./raw/sso/subdistrict.txt");
			if(!raw.exists()) throw new RuntimeException(raw.getAbsolutePath() + " doens't exists");
			br = new BufferedReader( new InputStreamReader(new FileInputStream(raw),"TIS-620"));
			
			String s = null;
			while((s = br.readLine())!= null) {
				String [] vals = s.split("\\|");
				rawSubDistrict.put(vals[1], vals[0]);
				
			}
		}finally{
			try{br.close(); }catch(Exception ex){}
		}
		System.out.println(rawSubDistrict.size());
//		for(Entry<String, String> e : rawSubDistrict.entrySet()){
//			System.out.println(e.getKey() + "|" + e.getValue());
//		}
	}
	public static void _3_readDistrict() throws IOException {
		BufferedReader br = null;
		try{
			File raw = new File("./raw/sso/district.txt");
			if(!raw.exists()) throw new RuntimeException(raw.getAbsolutePath() + " doens't exists");
			br = new BufferedReader( new InputStreamReader(new FileInputStream(raw),"TIS-620"));
			
			String s = null;
			while((s = br.readLine())!= null) {
				String [] vals = s.split("\\|");
				rawDistrict.put(vals[1], vals[0]);
				
			}
		}finally{
			try{br.close(); }catch(Exception ex){}
		}
//		System.out.println(rawDistrict.size());
//		for(Entry<String, String> e : rawDistrict.entrySet()){
//			System.out.println(e.getKey() + "|" + e.getValue());
//		}
	}

	public static void _2_readProvince() throws IOException {
		BufferedReader br = null;
		try{
			File raw = new File("./raw/sso/province.txt");
			if(!raw.exists()) throw new RuntimeException(raw.getAbsolutePath() + " doens't exists");
			br = new BufferedReader( new InputStreamReader(new FileInputStream(raw),"TIS-620"));
			
			String s = null;
			while((s = br.readLine())!= null) {
				String [] vals = s.split("\\|");
				rawProvince.put(vals[1], vals[0]);
				
			}
		}finally{
			try{br.close(); }catch(Exception ex){}
		}
//		System.out.println(rawProvince.size());
//		for(Entry<String, String> e : rawProvince.entrySet()){
//			System.out.println(e.getKey() + "|" + e.getValue());
//		}
	}

	public static void _1_readRawZipcode() throws Exception {
		
		BufferedReader br = null;
		try{
			File raw = new File("./raw/sample/raw_database_modifed.json");
			if(!raw.exists()) throw new RuntimeException(raw.getAbsolutePath() + " doens't exists");
			br = new BufferedReader( new InputStreamReader(new FileInputStream(raw),"TIS-620"));
			JsonNode jsonNode = new ObjectMapper().readTree(br);
			
			
			Iterator<JsonNode> it  = jsonNode.elements();
//			System.out.println(jsonNode.size());
			while(it.hasNext()) {
				JsonNode node = it.next();
				String subdistrict  = node.get("district").asText();
				String district  = node.get("amphoe").asText();
				Integer districtCode  = node.get("amphoe_code").asInt();
				String province  = node.get("province").asText();
				Integer zipcode  = node.get("zipcode").asInt();
				Integer province_code  = node.get("province_code").asInt();
//				if(districtCode == 1050)
//					System.out.println(district + "|" + subdistrict);
				if(province_code == 10) district = "ࢵ"+ district;
				_1_1_addRawZipCode(province_code,province, district, subdistrict, zipcode);
			}
			
//			System.out.println(rawPostal.size());
//			for(Entry<String, String> e : rawPostal.entrySet()){
//				if(district.)
//					System.out.println(e.getKey() + "|" + e.getValue());
//			}
			
		}finally{
			try{br.close(); }catch(Exception ex){}
		}
		
	}
	
	public static String createKey(String province_code,String province,String district, String subdistrict){
//		String key =province_code + "@"+ province + "@" + district + "@"+  subdistrict;
		String key = province + "@" + district + "@"+  subdistrict;
		return key;
	}
	public static void _1_1_addRawZipCode(Integer province_code,String province,String district, String subdistrict,Integer zipcode){
		
		String key = createKey(String.valueOf(province_code), province, district, subdistrict);
//		String key =district + "@" + province_code + "@"+ province + "@" + amphoe;
		
		String value = rawPostal.get(key);
		if(value == null ){ 
			value = String.valueOf(zipcode);
		}
		else  {
			value = value + ", " + String.valueOf(zipcode);
//			System.out.println(key + "," + value);
		}
		
		rawPostal.put(key, value);
	}
}
