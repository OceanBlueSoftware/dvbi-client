/**
 * ORB Software. Copyright (c) 2026 Ocean Blue Software Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.orbtv.dvbiclient.model;

import android.content.ContentValues;

import org.xmlpull.v1.XmlPullParser;

public class RelatedMaterial {
    public static final String DB_COLUMN_HOW_RELATED_HREF = "how_related_href";
    public static final String DB_COLUMN_MEDIA_LOCATOR_URI = "media_locator_uri";
    public static final String DB_COLUMN_MEDIA_LOCATOR_CONTENT_TYPE = "media_locator_content_type";
    private String mHowRelatedHref;
    private String mMediaLocatorUri;
    private String mMediaLocatorContentType;
    private String mHtmlLocatorUri;
    private String mHtmlLocatorContentType;
    private String mAitLocatorUri;
    private String mAitLocatorContentType;

    private RelatedMaterial() { }

    public String getHowRelatedHref() {
        return mHowRelatedHref;
    }

    /**
     * Service-instance / 1.x launch URI. When HTML and XML AIT are both
     * signalled, the AIT wins so {@link org.orbtv.dvbiclient.DvbIChannelAdapter}
     * still finds the HbbTV application.
     */
    public String getMediaLocatorUri() {
        if (mAitLocatorUri != null && !mAitLocatorUri.isEmpty()) {
            return mAitLocatorUri;
        }
        if (mHtmlLocatorUri != null && !mHtmlLocatorUri.isEmpty()) {
            return mHtmlLocatorUri;
        }
        return mMediaLocatorUri;
    }

    public String getMediaLocatorContentType() {
        if (mAitLocatorUri != null && !mAitLocatorUri.isEmpty()) {
            return mAitLocatorContentType;
        }
        if (mHtmlLocatorUri != null && !mHtmlLocatorUri.isEmpty()) {
            return mHtmlLocatorContentType;
        }
        return mMediaLocatorContentType;
    }

    /**
     * Type 4.x install / withdraw / renew. Official lists signal generic HTML
     * and an XML AIT in one RelatedMaterial; Setup already hosts HTML 4.1, so
     * prefer that locator when both are present.
     */
    public String getType4xLaunchUri() {
        if (mHtmlLocatorUri != null && !mHtmlLocatorUri.isEmpty()) {
            return mHtmlLocatorUri;
        }
        return getMediaLocatorUri();
    }

    public String getType4xLaunchContentType() {
        if (mHtmlLocatorUri != null && !mHtmlLocatorUri.isEmpty()) {
            return mHtmlLocatorContentType != null ? mHtmlLocatorContentType : "text/html";
        }
        return getMediaLocatorContentType();
    }

    public boolean isType4xHtml() {
        return isHtmlContentType(getType4xLaunchContentType())
                || isHtmlLocatorUri(getType4xLaunchUri());
    }

    public boolean isXmlAitContentType() {
        return "application/vnd.dvb.ait+xml".equalsIgnoreCase(getMediaLocatorContentType());
    }

    /** HbbTV Annex O.3 generic HTML (not an HbbTV XML AIT). */
    public boolean isGenericHtmlContentType() {
        return isHtmlContentType(getMediaLocatorContentType());
    }

    private static boolean isHtmlContentType(String contentType) {
        return "text/html".equalsIgnoreCase(contentType)
                || "application/xhtml+xml".equalsIgnoreCase(contentType);
    }

    private static boolean isHtmlLocatorUri(String uri) {
        if (uri == null) {
            return false;
        }
        String path = uri.toLowerCase();
        int q = path.indexOf('?');
        if (q >= 0) {
            path = path.substring(0, q);
        }
        return path.endsWith(".html") || path.endsWith(".htm")
                || path.endsWith(".xhtml") || path.endsWith(".html5");
    }

    @Override
    public String toString() {
        return "- " + this.getClass().getSimpleName() + " -\n"
                + "howRelatedHref: " + this.mHowRelatedHref + "\n"
                + "mediaLocatorContentType: " + this.getMediaLocatorContentType() + "\n"
                + "mediaLocatorUri: " + this.getMediaLocatorUri();
    }

    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        values.put(RelatedMaterial.DB_COLUMN_HOW_RELATED_HREF, mHowRelatedHref);
        values.put(RelatedMaterial.DB_COLUMN_MEDIA_LOCATOR_URI, getMediaLocatorUri());
        values.put(RelatedMaterial.DB_COLUMN_MEDIA_LOCATOR_CONTENT_TYPE, getMediaLocatorContentType());
        return values;
    }

    public static RelatedMaterial parseFromXML(XmlPullParser xpp) throws Exception {
        RelatedMaterial relatedMaterial = new RelatedMaterial();
        int eventType = xpp.getEventType();
        while (!(eventType == XmlPullParser.END_TAG && "RelatedMaterial".equals(xpp.getName()))) {
            if (eventType == XmlPullParser.START_TAG) {
                switch (xpp.getName()) {
                    case "HowRelated":
                        relatedMaterial.mHowRelatedHref = xpp.getAttributeValue(null, "href");
                        break;
                    case "MediaLocator":
                        parseMediaLocator(xpp, relatedMaterial);
                        break;
                    case "MediaUri":
                        relatedMaterial.mMediaLocatorContentType =
                                xpp.getAttributeValue(null, "contentType");
                        break;
                }
            }
            eventType = xpp.next();
        }
        return relatedMaterial;
    }

    private void applyLocator(String uri, String contentType) {
        if (uri != null && !uri.isEmpty()) {
            mMediaLocatorUri = uri;
        }
        if (contentType != null && !contentType.isEmpty()) {
            mMediaLocatorContentType = contentType;
        }
        if (isHtmlContentType(contentType) || isHtmlLocatorUri(uri)) {
            if (mHtmlLocatorUri == null || mHtmlLocatorUri.isEmpty()) {
                mHtmlLocatorUri = uri;
                mHtmlLocatorContentType = contentType != null ? contentType : "text/html";
            }
        }
        if ("application/vnd.dvb.ait+xml".equalsIgnoreCase(contentType)) {
            mAitLocatorUri = uri;
            mAitLocatorContentType = contentType;
        }
    }

    private static void parseMediaLocator(XmlPullParser xpp, RelatedMaterial relatedMaterial) throws Exception {
        int eventType = xpp.next();
        while (!(eventType == XmlPullParser.END_TAG && "MediaLocator".equals(xpp.getName()))) {
            if (eventType == XmlPullParser.START_TAG && "MediaUri".equals(xpp.getName())) {
                String contentType = xpp.getAttributeValue(null, "contentType");
                String uri = xpp.nextText();
                relatedMaterial.applyLocator(uri, contentType);
            }
            eventType = xpp.next();
        }
    }

    public static class Builder {
        private RelatedMaterial mInstance;
        public Builder() {
            mInstance = new RelatedMaterial();
        }
        public RelatedMaterial.Builder setHowRelatedHref(String value) {
            mInstance.mHowRelatedHref = value;
            return this;
        }
        public RelatedMaterial.Builder setMediaLocatorUri(String value) {
            mInstance.mMediaLocatorUri = value;
            return this;
        }
        public RelatedMaterial.Builder setMediaLocatorContentType(String value) {
            mInstance.mMediaLocatorContentType = value;
            return this;
        }
        public RelatedMaterial build() {
            RelatedMaterial instance = new RelatedMaterial();
            instance.mHowRelatedHref = mInstance.mHowRelatedHref;
            instance.applyLocator(mInstance.mMediaLocatorUri, mInstance.mMediaLocatorContentType);
            return instance;
        }
    }
}
