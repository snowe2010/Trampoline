package org.ernest.applications.trampoline.collectors;

import org.ernest.applications.trampoline.entities.Instance;
import org.ernest.applications.trampoline.entities.InstanceGitInfo;
import org.ernest.applications.trampoline.services.EcosystemManager;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

@Component
public class InstanceInfoCollector {

    private static final Logger LOGGER = LoggerFactory.getLogger(TraceCollector.class);

    @Autowired
    EcosystemManager ecosystemManager;

    public InstanceGitInfo getInfo(String idInstance) {
        InstanceGitInfo info = new InstanceGitInfo();

        Instance instance = ecosystemManager.getEcosystem().getInstances().stream().filter(i -> i.getId().equals(idInstance)).findAny().get();
        info.setPomLocation(instance.getPomLocation());
        try {
            JSONObject infoJson = new JSONObject(new RestTemplate().getForObject("http://127.0.0.1:" + instance.getPort() + instance.getActuatorPrefix() + "/info", String.class));

            info.setBranch(infoJson.getJSONObject("git").get("branch").toString());
            info.setCommitMessage(infoJson.getJSONObject("git").getJSONObject("commit").getJSONObject("message").get("full").toString());
            info.setCommitOwner(infoJson.getJSONObject("git").getJSONObject("commit").getJSONObject("user").get("name").toString() + "["+infoJson.getJSONObject("git").getJSONObject("commit").getJSONObject("user").get("email").toString()+"]");

            Long timestamp = Long.valueOf(infoJson.getJSONObject("git").getJSONObject("commit").get("time").toString());
            info.setCommitDate(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format( new Date(new Timestamp(timestamp).getTime())));
        }catch (Exception e){
            info.setBranch("-");
            info.setCommitMessage("-");
            info.setCommitOwner("-");
            info.setCommitDate("-");

            LOGGER.error("Not possible to retrieve git info for instance: ["+instance.getId()+"] hosted on port: ["+instance.getPort()+"]");
        }
        return info;
    }
}
