package test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.*;
import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONObject;


public class test {

	private static final String PHOTOS_ENDPOINT = "https://api.nasa.gov/mars-photos/api/v1/rovers/";
	private static final String MANIFEST_ENDPOINT = "https://api.nasa.gov/mars-photos/api/v1/manifests/";
	private static final String CURIOSITY_NAME = "Curiosity";
	private static final String OPPORTUNITY_NAME = "Opportunity";
	private static final String SPIRIT_NAME = "Spirit";
	private static final String MARTIAN_SOL = "?sol=";
	private static final String EARTH_DATE = "?earth_date=";
	private static final String PAGE_1 = "&page=";
	private static final String API_KEY = "api_key=b0haGjrtR178NTGSaOmBySxK3svgeacYs99Mnycx";
	private static final int PHOTOS_TO_GET = 10;

	@Test
	void test1() {
		ArrayList<String> photos = getFirstNPhotos(getPhotosInPage(MARTIAN_SOL+"1000", CURIOSITY_NAME, "1"), PHOTOS_TO_GET);
		for (int i = 0; i < photos.size(); i++) {
			System.out.println("The photo " + (i+1) + " taken by "  +CURIOSITY_NAME + " on Martian Sol 1000 is: " + photos.get(i));
		}
	}

	@Test
	void test2() {
		JSONObject roverManifest = getRoverManifest(MANIFEST_ENDPOINT, CURIOSITY_NAME);
		
		LocalDate launchDate = LocalDate.parse(roverManifest.get("launch_date").toString());
		//System.out.println("The launch date is: " + launchDate.toString());
		String earthDate = launchDate.plusDays(1000).toString();
		ArrayList<String> photos = getFirstNPhotos(getPhotosInPage(EARTH_DATE+earthDate, CURIOSITY_NAME, "1"), PHOTOS_TO_GET);
		for (int i = 0; i < photos.size(); i++) {
			System.out.println("The photo " + (i+1) + " taken by " + CURIOSITY_NAME + " on Martian Sol 1000 (Earth date = " + earthDate + ") is: " + photos.get(i));
		}

	}

	@Test
	void test3() {
		JSONObject roverManifest = getRoverManifest(MANIFEST_ENDPOINT, CURIOSITY_NAME);
		
		LocalDate launchDate = LocalDate.parse(roverManifest.get("launch_date").toString());
		String earthDate = launchDate.plusDays(1000).toString();

		//System.out.println("The launch date is: " + launchDate.toString());
		ArrayList<String> photosArraySolDate = getFirstNPhotos(getPhotosInPage(MARTIAN_SOL+"1000", CURIOSITY_NAME, "1"), PHOTOS_TO_GET);
		ArrayList<String> photosArrayEarthDate = getFirstNPhotos(getPhotosInPage(EARTH_DATE+earthDate, CURIOSITY_NAME, "1"), PHOTOS_TO_GET);

		// This will work because both arrays have the same size()
		for(int i = 0; i<photosArraySolDate.size(); i++){
			System.out.println("The photo " + i + " using Sol 1000 date is " + photosArraySolDate.get(i));
			System.out.println("And the photo " + i + " using earth date "+ earthDate + " is " + photosArrayEarthDate.get(i));
			System.out.println();
		}
	}
	
	@Test
	void test4() {
		int photosByOtherCameras = 0;
		
		Map<String,Integer> opportunityPhotos = getPhotosByRover(OPPORTUNITY_NAME, MARTIAN_SOL+"1000");
		for (String key: opportunityPhotos.keySet()) {
			System.out.println(OPPORTUNITY_NAME+ "'s camera " + key.toString() + " took this pictures: " + opportunityPhotos.get(key));
			photosByOtherCameras += opportunityPhotos.get(key).intValue();
		}
		
		Map<String,Integer> spiritCuriosityPhotos = getPhotosByRover(SPIRIT_NAME, MARTIAN_SOL+"1000");
		for (String key: spiritCuriosityPhotos.keySet()) {
			System.out.println(SPIRIT_NAME + "'s camera " + key.toString() + " took this pictures: " + spiritCuriosityPhotos.get(key));
			photosByOtherCameras += spiritCuriosityPhotos.get(key).intValue();
		}
		System.out.println();
		System.out.println("The amount of photos taken by all the non-Curiosity cameras is: " + photosByOtherCameras);
		System.out.println();
		
		Map<String,Integer> curiosityPhotos = getPhotosByRover(CURIOSITY_NAME, MARTIAN_SOL+"1000");
		for (String key: curiosityPhotos.keySet()) {
			System.out.println(CURIOSITY_NAME + "'s camera " + key.toString() + " took this pictures: " + curiosityPhotos.get(key));
			if(curiosityPhotos.get(key).intValue() <= 10*photosByOtherCameras) {
				System.out.println("The camera " + key.toString() + " took the same or less photos than 10 times of all the others combined");
			}
			else{
				System.out.println("The camera " + key.toString() + " took more photos than 10 times of all the others combined");
			}
			System.out.println();
			
		}
	}
	
	Map<String,Integer> getPhotosByRover(String rover, String date){
		JSONArray photosArray = getPhotosInPage(date, rover, "all");
		//System.out.println("getPhotosByRover photosArray:" + photosArray);
		String roverName;
		String cameraName; 
		Map<String,Integer> map = new HashMap<String,Integer>();          
		for(int i = 0; i < photosArray.length(); i++){
			JSONObject photo = (JSONObject)photosArray.getJSONObject(i);
			roverName = ((JSONObject)photo.get("rover")).get("name").toString();
			
			if (roverName.equals(rover)) { 
				cameraName = ((JSONObject)photo.get("camera")).get("name").toString(); 
				if (!map.containsKey(cameraName)) {
			        map.put(cameraName, 1);
			    } else {
			        int currentCount = map.get(cameraName);
			        currentCount++;
			        map.put(cameraName, currentCount);
			    }
			    //System.out.println("The camera of " + roverName + " is: " + cameraName + " and it took this pictures: " + map.get(cameraName));
			}
		}
		//System.out.println("Returning map: " + map);
		return map;
	}
	
	ArrayList<String> getFirstNPhotos(JSONArray photos, int n) {
		ArrayList<String> res = new ArrayList<String>();
		for(int i = 0; i < n; i++){
			String photoURL = photos.getJSONObject(i).getString("img_src");
			//System.out.println("The photo " + (i+1) + " is: " + photoURL);
			res.add(photoURL);
		}
		return res;
	}
	
	JSONArray getPhotosInPage(String dateParam, String roverName, String page) {
		String request_endpoint;
		String roverNameEndpoint = roverName.substring(0,1).toUpperCase() + roverName.substring(1);
		if(page.equals("all")) {
			request_endpoint = PHOTOS_ENDPOINT+roverNameEndpoint+"/photos"+dateParam+"&"+API_KEY;
		}
		else{
			request_endpoint = PHOTOS_ENDPOINT+roverNameEndpoint+"/photos"+dateParam+PAGE_1+page+"&"+API_KEY;
		}
		
		//System.out.println("Requesting: " + request_endpoint);
		Response resp = get(request_endpoint);
		int statusCode = resp.getStatusCode();
		Assert.assertEquals(statusCode, 200);
		
		String body = resp.getBody().asString();
		//System.out.println("Resp is: " + body);
		JSONObject json = new JSONObject(body);
		JSONArray photosJson = json.getJSONArray("photos");

		//System.out.println("Returning: " + photosJson);

		return photosJson;
	}
	
	JSONObject getRoverManifest(String endpoint, String roverName) {
		String request_endpoint = MANIFEST_ENDPOINT + roverName + "?"+API_KEY;
		
		//System.out.println("Requesting: " + request_endpoint);
		Response resp = get(request_endpoint);
		int statusCode = resp.getStatusCode();
		Assert.assertEquals(statusCode, 200);
		
		String body = resp.getBody().asString();
		//System.out.println("Resp is: " + body);
		JSONObject json = new JSONObject(body);
		JSONObject manifestJson = json.getJSONObject("photo_manifest");

		return manifestJson;
	}
}