package org.eedri.jenkins.plugins;

import hudson.Launcher;
import hudson.Extension;
import hudson.util.FormValidation;
import hudson.model.AbstractBuild;
import hudson.model.AutoCompletionCandidates;
import hudson.model.BuildListener;
import hudson.model.AbstractProject;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.QueryParameter;
import org.eedri.jenkins.plugins.ForemanHttpController;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * Foreman Plugin {@link Builder}.
 *
 * <p>
 * When the user configures the project and enables this builder,
 * {@link DescriptorImpl#newInstance(StaplerRequest)} is invoked
 * and a new {@link Foreman} is created. The created
 * instance is persisted to the project configuration XML by using
 * XStream, so this allows you to use instance fields (like {@link #name})
 * to remember the configuration.
 *
 * <p>
 * When a build is performed, the {@link #perform(AbstractBuild, Launcher, BuildListener)}
 * method will be invoked.
 *
 * @author Eyal Edri
 */
public class Foreman extends Builder {

    private final String vmName;
    private final String hostgroup;
    private final String cloudType;
    private final Boolean deleteVm;

    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public Foreman(String vmName, String hostgroup, String cloudType, Boolean deleteVm) {
        this.vmName = vmName;
        this.hostgroup = hostgroup;
        this.cloudType = cloudType;
        this.deleteVm = deleteVm;
    }

    /**
     * We'll use this from the <tt>config.jelly</tt>.
     */
    public String getVmName() {
        return vmName;
    }

    public String getHostgroup() {
        return hostgroup;
    }
    
    public String getCloudType() {
        return cloudType;
    }
    
    public Boolean getDeleteVm() {
        return deleteVm;
    }
    
    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
        listener.getLogger().println(getVmName());
        listener.getLogger().println(getHostgroup());
        listener.getLogger().println(getCloudType());
        listener.getLogger().println(getDeleteVm());
        //String url = getDescriptor().getForemanUrl();
        //listener.getLogger().println("foreman url, "+url+"!");
        return true;
    }

    // Overridden for better type safety.
    // If your plugin doesn't really define any property on Descriptor,
    // you don't have to do this.
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

	/**
     * Descriptor for {@link Foreman}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     *
     */
    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        /**
         * To persist global configuration information,
         * simply store it in a field and call save().
         *
         * <p>
         * If you don't want fields to be persisted, use <tt>transient</tt>.
         */
        private String url;
        private int port;
        private String username;
        private String password;

        /**
         * Performs on-the-fly validation of the form field 'name'.
         *
         * @param value
         *      This parameter receives the value that the user has typed.
         * @return
         *      Indicates the outcome of the validation. This is sent to the browser.
         */
        //TODO: add proper fqdn validation
        public FormValidation doCheckUrl(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error("Please enter foreman URL");
            // ** TODO: add URL validation here
            if (value.length() < 4)
                return FormValidation.warning("Isn't the URL too short?");
            return FormValidation.ok();
        }

        //TODO: add real foreman server validation
        public FormValidation doTestConnection(@QueryParameter("url") final String url,
        		@QueryParameter("port") final int port,
                @QueryParameter("username") final String username, 
                @QueryParameter("password") final String password)
                		throws IOException, ServletException {
            try {
                ForemanHttpController foremanHttpController = 
                		new ForemanHttpController(url, port, username, password);
                
                // run simple GET with basic auth to check server connection
                foremanHttpController.validateServerConn();
                return FormValidation.ok("Success");
            } catch (Exception e) {
                return FormValidation.error("Client error : "+e.getMessage());
            }
        }
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types
            return true;
        }
        
        public AutoCompletionCandidates doAutoCompleteHostgroup(@QueryParameter String value) {
            AutoCompletionCandidates c = new AutoCompletionCandidates();
            for (String hostgroup : HOSTGROUPS)
                if (hostgroup.toLowerCase().startsWith(value.toLowerCase()))
                    c.add(hostgroup);
            return c;
        }
        
        public AutoCompletionCandidates doAutoCompleteCloudType(@QueryParameter String value) {
            AutoCompletionCandidates c = new AutoCompletionCandidates();
            for (String cloud : CLOUDS)
                if (cloud.toLowerCase().startsWith(value.toLowerCase()))
                    c.add(cloud);
            return c;
        }

        /**
         * This human readable name is used in the configuration screen.
         */
        public String getDisplayName() {
            return "Create a Virtual Machine";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            // To persist global configuration information,
            // set that to properties and call save().
            this.url= formData.getString("url");
            this.port = formData.getInt("port");
            this.username = formData.getString("username");
            this.password = formData.getString("password");
            
            // ^Can also use req.bindJSON(this, formData);
            //  (easier when there are many fields; need set* methods for this, like setUseFrench)
            save();
            return super.configure(req,formData);
        }

		public String getUrl() {
			return url;
		}

		public String getUsername() {
			return username;
		}

		public String getPassword() {
			return password;
		}
		
		public int getPort() {
			return port;
		}

		public void setPassword(String password) {
			this.password = password;
		}

    }
    
    //TODO: read this from foreman 
    private static final String[] HOSTGROUPS = new String[]{
        "RHEL-63",
        "FEDORA-16",
        "FEDROA-17"
    };
    
    //TODO: read this from foreman
    private static final String[] CLOUDS = new String[]{
        "ovirt",
        "libvirt",
        "amazon"
    };
}

