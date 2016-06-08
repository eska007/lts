package com.kaist.lts;

import java.util.HashMap;

/**
 * Created by Administrator on 2016-06-08.
 */
public class ItemFilter {
    static HashMap<String, String> allowedRequestColumn = null;
    static HashMap<String, String> allowedMemberColumn = null;
    static boolean isInitialized = false;
    static void init() {
        /* "subject", "id", "doc_type", "source_language", "target_language", "pages", "level", "biding", "cost",
        "is_paid", "is_reviewed", "source_doc_path", "request_date", "due_date", "requester_id", "translator_candidate_list",
        "reviewer_candidate_list", "translator_id", "reviewer_id", "translated_doc_path", "reviewed_doc_path", "final_doc_path"*/
        allowedRequestColumn = new HashMap<String, String>();
        allowedRequestColumn.put("doc_type", "문서종류");
        allowedRequestColumn.put("source_language", "원본언어");
        allowedRequestColumn.put("target_language", "번역어");
        allowedRequestColumn.put("pages", "페이지수");
        allowedRequestColumn.put("level", "번역레벨");
        allowedRequestColumn.put("due_date", "기한");
        allowedRequestColumn.put("translator_candidate_list", "번역가후보");
        allowedRequestColumn.put("reviewer_candidate_list", "감수자후보");
        allowedRequestColumn.put("translator_id", "번역가");
        allowedRequestColumn.put("reviewer_id", "감수자");
        allowedRequestColumn.put("requester_id", "의뢰자");

        allowedRequestColumn.put("source_doc_path", "원본자료");
        allowedRequestColumn.put("translated_doc_path", "중간번역자료");
        allowedRequestColumn.put("reviewed_doc_path", "감수자료");
        allowedRequestColumn.put("final_doc_path", "최종번역자료");

        /* ("id", "password", "first_name", "family_name", "email", "phone", "country", "address", "sex", "birthday", "user_mode",
        "degree", "college", "graduate", "certification", "resume", "account", "worklist", "new_request", "language", "_notified_new_request", "_applied_request") */
        allowedMemberColumn = new HashMap<String, String>();
        allowedMemberColumn.put("first_name", "이름");
        allowedMemberColumn.put("family_name", "성");
        allowedMemberColumn.put("email", "이메일");
        allowedMemberColumn.put("phone", "전화");
        allowedMemberColumn.put("degree", "학위");
        allowedMemberColumn.put("college", "대학교");
        allowedMemberColumn.put("language", "전문언어");
        allowedMemberColumn.put("graduate", "대학원");
        allowedMemberColumn.put("worklist", "업무이력");
    }
    static String GetAllowedTermForRequestColumn(String key, int user_mode) {
        if (!isInitialized) {
            init();
            isInitialized = true;
        }

        if (!allowedRequestColumn.containsKey(key))
            return null;
        switch(user_mode) {
            case ProfileManager.USER_MODE.REQUESTER:
                if(key.equals("translated_doc_path")
                        || key.equals("reviewed_doc_path"))
                    return null;
            case ProfileManager.USER_MODE.TRANSLATOR:
                break;
            case ProfileManager.USER_MODE.REVIEWER:
                if(key.equals("final_doc_path"))
                    return null;
                break;
            default:
                break;
        }
        return allowedRequestColumn.get(key);
    }

    static String GetAllowedTermForMemberColumn(String key, int user_mode) {
        if (!isInitialized) {
            init();
            isInitialized = true;
        }

        if (!allowedMemberColumn.containsKey(key))
            return null;

        switch(user_mode) {
            case ProfileManager.USER_MODE.REQUESTER:
                break;
            case ProfileManager.USER_MODE.TRANSLATOR:
            case ProfileManager.USER_MODE.REVIEWER:
                break;
            default:
                break;
        }
        return allowedMemberColumn.get(key);
    }
}
