package cn.magicvector.common.basic.mongomq;

public class Message {
    private String id;
    private String topic;
    private Object payload;
    private long triggerTime;
    private long createTime;
    private boolean processed;
    private String processingInstance;
    private long processStartTime;
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }
    
    public Object getPayload() { return payload; }
    public void setPayload(Object payload) { this.payload = payload; }
    
    public long getTriggerTime() { return triggerTime; }
    public void setTriggerTime(long triggerTime) { this.triggerTime = triggerTime; }
    
    public long getCreateTime() { return createTime; }
    public void setCreateTime(long createTime) { this.createTime = createTime; }
    
    public boolean isProcessed() { return processed; }
    public void setProcessed(boolean processed) { this.processed = processed; }
    
    public String getProcessingInstance() { return processingInstance; }
    public void setProcessingInstance(String processingInstance) { this.processingInstance = processingInstance; }
    
    public long getProcessStartTime() { return processStartTime; }
    public void setProcessStartTime(long processStartTime) { this.processStartTime = processStartTime; }
}