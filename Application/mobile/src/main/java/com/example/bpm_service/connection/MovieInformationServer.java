package com.example.bpm_service.connection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

public class MovieInformationServer {
    private String url = "";
    private String data = "";

    public MovieInformationServer(String IP){
        this.url = IP;
    }

    //검색
    public String search(String query){
        try {
            url += "8081/searchmovie?query="+query;
            return new GetTask().execute(url).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
            return "ExecutionError";
        } catch (InterruptedException e) {
            e.printStackTrace();
            return "InterruptedError";
        }
    }

    // 영화 순위
    public String hotMovieRank(){
        try {
            url += "8081/hotmovierank";
            return new GetTask().execute(url).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
            return "ExecutionError";
        } catch (InterruptedException e) {
            e.printStackTrace();
            return "InterruptedError";
        }
    }

    // 상세 정보
    public JSONObject getInfo(String title, String userId){
        try{
            url += "8081/movieinfo?userId="+userId+"&title="+title;
            data = new GetTask().execute(url).get();

            JSONArray json = new JSONArray(data);
            JSONObject obj = (JSONObject)json.get(0);

            return obj;

        }catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // 영화 좋아요 상태 변경
    public String changeLove(String userId, String title, String state){
        try{
            url += "8081/changelove?userId="+userId+"&title="+title+"&state="+state;
            return new GetTask().execute(url).get();
        }catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // 영화 좋아요 목록
    public String getLoveMovie(String userId){
        try {
            url += "8081/getlovemovie?userId=" + userId;
            return new GetTask().execute(url).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
            return "ExecutionError";
        } catch (InterruptedException e) {
            e.printStackTrace();
            return "InterruptedError";
        }
    }

    // 시청한 영화 목록
    public String getWatchMovie(String userId){
        try {
            url += "8081/getwatchmovie?userId=" + userId;
            return new GetTask().execute(url).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
            return "ExecutionError";
        } catch (InterruptedException e) {
            e.printStackTrace();
            return "InterruptedError";
        }
    }
}
