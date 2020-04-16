package com.hsj.demo.service;

import com.hsj.demo.model.User;
import org.apache.derby.impl.store.access.btree.SearchParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author sergei
 * @create 2020-04-15
 */
@Service
public class SearchService {
    private User user;

//    @Autowired
//    public SearchService(User user){
//        this.user = user;
//    }
    public List<User> search(String searchType, List<String> keywords) {
        return null;
    }
//    public List<User> search(String searchType, List<String>
//            keywords) {
//        List<SearchParameters> searches = keywords.stream()
//                .map(taste -> createSearchParam(searchType, taste))
//                .collect(Collectors.toList());
//        List<User> results = searches.stream()
//                .map(params -> user.searchOperations().
//                        search(params)) .flatMap(searchResults -> searchResults.getTweets().
//                        stream())
//                .collect(Collectors.toList());
//        return results;
//    }
//    private SearchParameters.ResultType getResultType(String
//                                                              searchType) {
//        for (SearchParameters.ResultType knownType : SearchParameters.
//                ResultType.values()) {
//            if (knownType.name().equalsIgnoreCase(searchType)) {
//                return knownType;
//            }
//        }
//        return SearchParameters.ResultType.RECENT;
//    }
//    private SearchParameters createSearchParam(String searchType,
//                                               String taste) {
//        SearchParameters.ResultType resultType =
//                getResultType(searchType);
//        SearchParameters searchParameters = new
//                SearchParameters(taste);
//        searchParameters.resultType(resultType);
//        searchParameters.count(3);
//        return searchParameters;
//    }

}

