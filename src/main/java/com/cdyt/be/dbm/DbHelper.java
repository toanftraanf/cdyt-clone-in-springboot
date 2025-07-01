package com.cdyt.be.dbm;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Component;

import java.sql.Types;
import java.util.*;

@Component
public class DbHelper {
  private final JdbcTemplate jdbcTemplate;

  public DbHelper(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  // === GENERIC EXECUTOR ===
  public <T> DbResult<T> execStore(
      String procName,
      Map<String, ?> inParams,
      Map<String, Integer> outParams,
      QueryType queryType,
      RowMapper<?> rowMapper
  ) {
    try {
      SimpleJdbcCall call = new SimpleJdbcCall(jdbcTemplate)
          .withProcedureName(procName);

      // OUT param declaration
      if (outParams != null) {
        for (var entry : outParams.entrySet()) {
          call.declareParameters(new SqlOutParameter(entry.getKey(), entry.getValue()));
        }
      }

      // ResultSet mapping
      if (rowMapper != null && (queryType == QueryType.GET_RESULT_SET || queryType == QueryType.RESULT_SET_AND_OUTPUTS)) {
        call.returningResultSet("result", rowMapper);
      }

      Map<String, Object> params = (inParams == null) ? new HashMap<>() : new HashMap<>(inParams);
      Map<String, Object> result = call.execute(params);

      switch (queryType) {
        case NO_OUTPUT:
          return new DbResult<>("", null);
        case WITH_OUTPUTS:
          Map<String, Object> outResult = new HashMap<>();
          if (outParams != null) {
            for (String key : outParams.keySet()) {
              outResult.put(key, result.get(key));
            }
          }
          return new DbResult<>("", (T) outResult);
        case GET_RESULT_SET:
          List<?> list = (List<?>) result.get("result");
          return new DbResult<>("", (T) list);
        case RESULT_SET_AND_OUTPUTS:
          Map<String, Object> combined = new HashMap<>();
          if (rowMapper != null)
            combined.put("result", result.get("result"));
          if (outParams != null) {
            for (String key : outParams.keySet()) {
              combined.put(key, result.get(key));
            }
          }
          return new DbResult<>("", (T) combined);
        default:
          return new DbResult<>("Unknown QueryType", null);
      }
    } catch (Exception e) {
      return new DbResult<>(e.getMessage(), null);
    }
  }

  // === CONVENIENCE SHORTCUTS ===

  // Get list of models from a procedure
  public <T> DbResult<List<T>> getList(String procName, Map<String, ?> inParams, RowMapper<T> rowMapper) {
    return execStore(procName, inParams, null, QueryType.GET_RESULT_SET, rowMapper);
  }

  // Get one model from a procedure
  public <T> DbResult<T> getOne(String procName, Map<String, ?> inParams, RowMapper<T> rowMapper) {
    DbResult<List<T>> listResult = getList(procName, inParams, rowMapper);
    if (listResult.hasError()) {
      return new DbResult<>(listResult.getError(), null);
    }
    List<T> dataList = listResult.getData();
    return new DbResult<>("", (dataList != null && !dataList.isEmpty()) ? dataList.get(0) : null);
  }

  // Execute update/insert/delete with no output
  public DbResult<Void> execUpdate(String procName, Map<String, ?> inParams) {
    return execStore(procName, inParams, null, QueryType.NO_OUTPUT, null);
  }

  // Execute and return only output parameters
  public DbResult<Map<String, Object>> execOutputs(String procName, Map<String, ?> inParams, Map<String, Integer> outParams) {
    return execStore(procName, inParams, outParams, QueryType.WITH_OUTPUTS, null);
  }

  // Execute and return both outputs and a result set
  public <T> DbResult<Map<String, Object>> execOutputsAndList(String procName, Map<String, ?> inParams, Map<String, Integer> outParams, RowMapper<T> rowMapper) {
    return execStore(procName, inParams, outParams, QueryType.RESULT_SET_AND_OUTPUTS, rowMapper);
  }
}
