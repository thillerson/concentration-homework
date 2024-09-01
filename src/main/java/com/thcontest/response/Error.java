package com.thcontest.response;

public record Error(
  String message,
  String detail
) {
}
