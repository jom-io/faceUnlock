package io.dz.faceunlock;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by zhaodu on 2018/4/13.
 */

public class FaceImgInfo {

    /**
     * image_id : rmqnu2IWzK4nI1O30IolYg==
     * request_id : 1523608691,ce740555-bae2-4e19-a781-d5eff71511c4
     * time_used : 388.0
     * faces : [{"attributes":{"facequality":{"threshold":70.1,"value":86.709}},"face_rectangle":{"width":59,"top":34,"left":45,"height":59},"face_token":"8d2e581a2e8346dbbccb468c032b032f"}]
     */

    private String image_id;
    private String request_id;
    private long time_used;
    private List<FacesBean> faces;
    private String error_message;
    /**
     * thresholds : {"1e-3":62.327,"1e-5":73.975,"1e-4":69.101}
     * confidence : 95.427
     */

    private String thresholds;
    private double confidence;


    public String getImage_id() {
        return image_id;
    }

    public void setImage_id(String image_id) {
        this.image_id = image_id;
    }

    public String getRequest_id() {
        return request_id;
    }

    public void setRequest_id(String request_id) {
        this.request_id = request_id;
    }

    public long getTime_used() {
        return time_used;
    }

    public void setTime_used(long time_used) {
        this.time_used = time_used;
    }

    public List<FacesBean> getFaces() {
        return faces;
    }

    public void setFaces(List<FacesBean> faces) {
        this.faces = faces;
    }

    public String getThresholds() {
        return thresholds;
    }

    public void setThresholds(String thresholds) {
        this.thresholds = thresholds;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public static class FacesBean {
        /**
         * attributes : {"facequality":{"threshold":70.1,"value":86.709}}
         * face_rectangle : {"width":59,"top":34,"left":45,"height":59}
         * face_token : 8d2e581a2e8346dbbccb468c032b032f
         */

        private AttributesBean attributes;
        private FaceRectangleBean face_rectangle;
        private String face_token;

        public AttributesBean getAttributes() {
            return attributes;
        }

        public void setAttributes(AttributesBean attributes) {
            this.attributes = attributes;
        }

        public FaceRectangleBean getFace_rectangle() {
            return face_rectangle;
        }

        public void setFace_rectangle(FaceRectangleBean face_rectangle) {
            this.face_rectangle = face_rectangle;
        }

        public String getFace_token() {
            return face_token;
        }

        public void setFace_token(String face_token) {
            this.face_token = face_token;
        }

        public static class AttributesBean {
            /**
             * facequality : {"threshold":70.1,"value":86.709}
             */

            private FacequalityBean facequality;

            public FacequalityBean getFacequality() {
                return facequality;
            }

            public void setFacequality(FacequalityBean facequality) {
                this.facequality = facequality;
            }

            public static class FacequalityBean {
                /**
                 * threshold : 70.1
                 * value : 86.709
                 */

                private double threshold;
                private double value;

                public double getThreshold() {
                    return threshold;
                }

                public void setThreshold(double threshold) {
                    this.threshold = threshold;
                }

                public double getValue() {
                    return value;
                }

                public void setValue(double value) {
                    this.value = value;
                }
            }
        }

        public static class FaceRectangleBean {
            /**
             * width : 59.0
             * top : 34.0
             * left : 45.0
             * height : 59.0
             */

            private double width;
            private double top;
            private double left;
            private double height;

            public double getWidth() {
                return width;
            }

            public void setWidth(double width) {
                this.width = width;
            }

            public double getTop() {
                return top;
            }

            public void setTop(double top) {
                this.top = top;
            }

            public double getLeft() {
                return left;
            }

            public void setLeft(double left) {
                this.left = left;
            }

            public double getHeight() {
                return height;
            }

            public void setHeight(double height) {
                this.height = height;
            }
        }
    }

    public String getError_message() {
        return error_message;
    }

    public void setError_message(String error_message) {
        this.error_message = error_message;
    }

//    public static class ThresholdsBean {
//        /**
//         * 1e-3 : 62.327
//         * 1e-5 : 73.975
//         * 1e-4 : 69.101
//         */
//
//        @SerializedName("1e-3")
//        private double _$1e3;
//        @SerializedName("1e-5")
//        private double _$1e5;
//        @SerializedName("1e-4")
//        private double _$1e4;
//
//        public double get_$1e3() {
//            return _$1e3;
//        }
//
//        public void set1e3(double _$1e3) {
//            this._$1e3 = _$1e3;
//        }
//
//        public double get_$1e5() {
//            return _$1e5;
//        }
//
//        public void set1e5(double _$1e5) {
//            this._$1e5 = _$1e5;
//        }
//
//        public double get_$1e4() {
//            return _$1e4;
//        }
//
//        public void set1e4(double _$1e4) {
//            this._$1e4 = _$1e4;
//        }
//    }
}
