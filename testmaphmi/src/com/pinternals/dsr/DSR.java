package com.pinternals.dsr;

import java.util.Map;

public class DSR {
	public int qty, max;
	String[] action, addInfo, userId, transId;
	int[] actionType, serviceType;
	long[] allocMem, cpuTime, dbTime, externalTime, receivedBytes, respTime, sentBytes, startTime, dbCalls, dbIO;
	DSR(int maxRecords) {
		qty = 0;
		max = maxRecords;
		cpuTime = new long[max];
		allocMem = new long[max];
		transId = new String[max];
	}
	public int feed(Map<String,StringBuilder> row) {
		cpuTime[qty] = Long.parseLong(row.get("cpuTime").toString());
		allocMem[qty] = Long.parseLong(row.get("allocMem").toString());
		transId[qty] = row.get("transId").toString();
		System.out.println(allocMem[qty] + " bytes, " + cpuTime[qty] + " ms\t\t" + transId[qty]);
		qty++;
		return qty;
	}
	
}

