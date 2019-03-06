package com.okta.android.results;

import com.okta.android.OktaException;

public class Result {

    private final OktaException error;

    Result(OktaException error) {
        this.error = error;
    }

    public static Result success() {
        return new Result(null);
    }

    public static Result error(OktaException error) {
        return new Result(error);
    }

    public boolean isSuccess(){
        return getError() == null;
    }

    public OktaException getError() {
        return error;
    }

}
