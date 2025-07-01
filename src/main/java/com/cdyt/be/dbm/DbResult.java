package com.cdyt.be.dbm;

public class DbResult<T> {
  private final String error;
  private final T data;

  public DbResult(String error, T data) {
    this.error = error;
    this.data = data;
  }

  public boolean hasError() {
    return error != null && !error.isEmpty();
  }

  public String getError() {
    return error;
  }

  public T getData() {
    return data;
  }
}
