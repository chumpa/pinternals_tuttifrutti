package com.sap.xi.tf;

import java.util.List;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import com.sap.aii.mapping.api.AbstractTrace;
import com.sap.aii.mapping.api.StreamTransformationException;
import com.sap.aii.mappingtool.tf7.rt.Container;
import com.sap.aii.mappingtool.tf7.rt.GlobalContainer;
import com.sap.engine.interfaces.messaging.api.APIAccess;
import com.sap.engine.interfaces.messaging.api.auditlog.AuditLogEntry;
import com.sap.engine.interfaces.messaging.api.MessageDirection;
import com.sap.engine.interfaces.messaging.api.MessageKey;
import com.sap.engine.interfaces.messaging.api.message.MessageAccess;
import com.sap.engine.interfaces.messaging.api.message.MessageAccessException;
import com.sap.engine.interfaces.messaging.api.message.MessageData;
import com.sap.ide.esr.tools.mapping.core.Argument;
import com.sap.ide.esr.tools.mapping.core.Cleanup;
import com.sap.ide.esr.tools.mapping.core.ExecutionType;
import com.sap.ide.esr.tools.mapping.core.Init;
import com.sap.ide.esr.tools.mapping.core.LibraryMethod;


public class _MM_ApiAccessDemo_  {



	@Init(description="") 
	public void init (
		 GlobalContainer container)  throws StreamTransformationException{
		
	}

	@Cleanup 
	public void cleanup (
		 GlobalContainer container)  throws StreamTransformationException{
		
	}

	@LibraryMethod(title="ApiAccessTest", description="", category="User-Defined", type=ExecutionType.SINGLE_VALUE) 
	public String ApiAccessTest (
		@Argument(title="")  String msgid,
		 Container container)  throws StreamTransformationException{
			Object o;
		InitialContext ctx;
		APIAccess a;
		AbstractTrace tr = container.getTrace();
		try {
			ctx = new InitialContext();
			o = ctx.lookup("com.sap.engine.interfaces.messaging.api");
			if (o==null) throw new StreamTransformationException("APIAccess isn't got");
			a = (APIAccess)o;
			tr.addInfo("APIAccess got well: " + a);
			
			
		} catch (NamingException e) {
			throw new StreamTransformationException("Can't detect JNDI. Error: " + e.getExplanation());
		}
		return "" + msgid + ";test";
	}

	@LibraryMethod(title="getMessageInfo", description="", category="User-Defined", type=ExecutionType.SINGLE_VALUE) 
	public String getMessageInfo (
		@Argument(title="")  String msgid,
		@Argument(title="INBOUND | OUTBOUND")  String dir,
		 Container container)  throws StreamTransformationException{
			Object o;
		InitialContext ctx;
		APIAccess a;
		StringBuilder sb = new StringBuilder("\n");
		AbstractTrace tr = container.getTrace();
		try {
			ctx = new InitialContext();
			o = ctx.lookup("com.sap.engine.interfaces.messaging.api");
			if (o==null) throw new StreamTransformationException("APIAccess isn't got");
			a = (APIAccess)o;
			tr.addInfo("APIAccess got well: " + a);
		} catch (NamingException e) {
			throw new StreamTransformationException("Can't detect JNDI. Error: " + e.getExplanation());
		}
		MessageAccess ma = a.getMessageAccess();
		MessageDirection d = MessageDirection.valueOf(dir);
		MessageKey mk = new MessageKey(msgid, d);
		tr.addInfo("Try to detect info about message:\t" + mk.toString());
		MessageData md = null;
		List<Object> l;
		try {
			md = ma.getMessageData(mk);
			l = ma.getLogEntries(mk);
		} catch (MessageAccessException e) {
			throw new StreamTransformationException(e.getMessage());
		}
		tr.addDebugMessage("Found message data:\t" + md.toString());
		sb.append(md.getConnectionName()).append("\n")
			.append(md.getAddress()).append("\n")
			.append(md.getCredential()).append("\n")
			.append(md.getErrorCategory()).append("\n")
			.append(md.getErrorCode()).append("\n")
			.append(md.getMessageSize()).append("\n")
			.append(md.getFromParty()+"|"+md.getFromService()).append("\n")
			
			;
		sb.append(md.getHeaders()).append("\n\n\n");
		
		for (Object x: l) {
			AuditLogEntry e = (AuditLogEntry)x;
			sb.append(e.getStatus() + " " + e.getTimestampAsString() + "\t" + e.getTextKey() + "\t");
			for (String y: e.getParams()) {
				sb.append(y+",");
			}
			sb.append("\n");
		}
		
		
//		ma.getLogEntries(mk);
		
//		int nodeId = a.getClusterNodeId();
//		
		
//		try {
//			MessageData md = ma.getMessageData(mk);
//			
//		} catch (MessageAccessException x) {
//			
//		}
		return sb.toString();
	}
}