package com.pinternals.testmap;


public class HmiClnt  { //extends HmiClientAdapter {

//	protected HmiClnt(boolean aRequiresSession, UriElement aServiceId,
//			String aUser, String aPassword, Language aLanguage,
//			ApplCompLevel aLevel) {
//		super(aRequiresSession, aServiceId, aUser, aPassword, aLanguage, aLevel);
//	}
//
//	@Override
//	public void allocateResources() throws HmiCoreException {
//	      if(!this.iAreResourcesAllocated) {
//	          this.iAreResourcesAllocated = true;
//	       }
//	}
//
//	@Override
//	public void freeResources() throws HmiCoreException {
//	      this.iAreResourcesAllocated = false;
//	}
//
//	@Override
//	public HmiResponse sendRequestAndReceiveResponse(HmiRequest aRequest)
//			throws HmiCoreException {
//	      String lRequestAsString = HmiRequest.render(aRequest);
//	      String lResponseAsString = this.sendRequestAndReceiveResponseViaHttp(lRequestAsString);
//	      GenericDataInstance lResponseAsGdi = GdiCharStreamer.stringToGdi(lResponseAsString);
//	      HmiResponse lResponse = HmiResponse.parseGdi(lResponseAsGdi);
//	      return lResponse;
//	}

}
