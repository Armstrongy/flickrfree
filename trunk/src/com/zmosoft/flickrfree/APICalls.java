package com.zmosoft.flickrfree;

import org.json.JSONException;
import org.json.JSONObject;

public class APICalls {
	
	public static JSONObject peopleGetInfo(String userid) {
		return RestClient.CallFunction("flickr.people.getInfo", new String[]{"user_id"}, new String[]{userid});
	}
	
	public static JSONObject peopleFindByUsername(String username) {
		return RestClient.CallFunction("flickr.people.findByUsername", new String[]{"username"}, new String[]{username});
	}

    public static String getNameFromNSID(String nsid) throws JSONException {
		JSONObject result = peopleGetInfo(nsid);
		return (result.has("person") && result.getJSONObject("person").has("username")
				&& result.getJSONObject("person").getJSONObject("username").has("_content")
				? result.getJSONObject("person").getJSONObject("username").getString("_content")
				: "");
    }
    
    public static String getNSIDFromName(String username) throws JSONException {
		JSONObject result = peopleFindByUsername(username);
		return (result.has("user") && result.getJSONObject("user").has("nsid")
				? result.getJSONObject("user").getString("nsid")
				: "");
    }
    
    public static JSONObject photosCommentsGetList(String photo_id) {
        return RestClient.CallFunction("flickr.photos.comments.getList",new String[]{"photo_id"},new String[]{photo_id});
    }
    
    public static JSONObject photosetsGetList(String userid) {
    	return RestClient.CallFunction("flickr.photosets.getList",new String[]{"user_id"},new String[]{userid});
    }
    
    public static JSONObject photosetsGetInfo(String setid) {
    	return RestClient.CallFunction("flickr.photosets.getInfo",new String[]{"photoset_id"},new String[]{setid});
    }

    public static JSONObject collectionsGetTree(String userid) {
		return RestClient.CallFunction("flickr.collections.getTree",new String[]{"user_id"},new String[]{userid});
    }

    public static JSONObject photosSearch(String userid, String tag) {
    	return RestClient.CallFunction("flickr.photos.search", new String[]{"user_id", "tags"}, new String[]{userid, tag});
    }

    public static JSONObject photosGetInfo(String photoid) {
        return RestClient.CallFunction("flickr.photos.getInfo",new String[]{"photo_id"},new String[]{photoid});
    }
    
    public static JSONObject photosGetExif(String photoid) {
        return RestClient.CallFunction("flickr.photos.getExif",new String[]{"photo_id"},new String[]{photoid});
    }
    
    public static JSONObject photosGetSizes(String photoid) {
        return RestClient.CallFunction("flickr.photos.getSizes",new String[]{"photo_id"},new String[]{photoid});
    }
    
    public static JSONObject photosGetAllContexts(String photoid) {
        return RestClient.CallFunction("flickr.photos.getAllContexts",new String[]{"photo_id"},new String[]{photoid});
    }
    
    public static JSONObject tagsGetListUser(String userid) {
    	return RestClient.CallFunction("flickr.tags.getListUser", new String[]{"user_id"}, new String[]{userid});
    }

    public static JSONObject groupsGetInfo(String groupid) {
        return RestClient.CallFunction("flickr.groups.getInfo",new String[]{"group_id"},new String[]{groupid});
    }
    
    public static JSONObject groupsSearch(String text, String perpage) {
        return RestClient.CallFunction("flickr.groups.search",new String[]{"text", "per_page"},new String[]{text, perpage});
    }
    
    public static String getGroupNameFromID(String groupid) {
    	String name = "";
    	
    	JSONObject group_info = groupsGetInfo(groupid);
    	try {
			if (group_info.has("group") && group_info.getJSONObject("group").has("name")
				&& group_info.getJSONObject("group").getJSONObject("name").has("_content")) {
				name = group_info.getJSONObject("group").getJSONObject("name").getString("_content");
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	//TODO Parse group_info to get the name of the group.
    	
    	return name;
    }
    
    public static String getGroupIDFromName(String groupname) {
    	String nsid = "";
    	
    	JSONObject group_info = groupsSearch(groupname, "1");
    	try {
			if (group_info.has("groups") && group_info.getJSONObject("groups").has("group")
				&& group_info.getJSONObject("groups").getJSONArray("group").length() > 0
				&& group_info.getJSONObject("groups").getJSONArray("group").getJSONObject(0).has("nsid")) {
				nsid = group_info.getJSONObject("groups").getJSONArray("group").getJSONObject(0).getString("nsid");
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	//TODO Parse group_info to get the name of the group.
    	
    	return nsid;
    }
    
    public static String getPhotoNameFromID(String photoid) {
    	String name = "";
    	
    	JSONObject photo_info = photosGetInfo(photoid);
    	try {
			if (photo_info.has("photo") && photo_info.getJSONObject("photo").has("title")
				&& photo_info.getJSONObject("photo").getJSONObject("title").has("_content")) {
				name = photo_info.getJSONObject("photo").getJSONObject("title").getString("_content");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
    	return name;
    }
    
    public static boolean authCheckToken() {
		JSONObject json_obj = RestClient.CallFunction("flickr.auth.checkToken",null,null);
		boolean rval = false;
		try {
			rval = json_obj.has("stat") && json_obj.getString("stat").equals("ok");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return rval;
    }

    public static JSONObject getFullToken(String minitoken) {
		return RestClient.CallFunction("flickr.auth.getFullToken",new String[]{"mini_token"},new String[]{minitoken},false);
    }
    
    public static void favoritesAdd(String photoid) {
    	RestClient.CallFunction("flickr.favorites.add", new String[]{"photo_id"}, new String[]{photoid});
    }

    public static void favoritesRemove(String photoid) {
    	RestClient.CallFunction("flickr.favorites.remove", new String[]{"photo_id"}, new String[]{photoid});
    }

}
