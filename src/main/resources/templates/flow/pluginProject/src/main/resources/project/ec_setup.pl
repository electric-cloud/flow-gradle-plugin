#
# The plugin is being promoted, create a property reference in the server's property sheet
# Data that drives the create step picker registration for this plugin.
#
no warnings qw/redefine/;
use XML::Simple;
use ElectricCommander::Util;

my %sayHello = (
    label       => "$shortPluginName - Say Hello",
    procedure   => "SayHello",
    description => "Say Hello to the world",
    category    => "Example"
);

\$batch->deleteProperty("/server/ec_customEditors/pickerStep/$shortPluginName - Say Hello");

@::createStepPickerSteps = (\\%sayHello);

my \$pluginName = "@PLUGIN_NAME@";
my \$pluginKey  = "@PLUGIN_KEY@";
if (\$promoteAction ne '') {
    my @objTypes = ('projects', 'resources', 'workspaces');
    my \$query    = \$commander->newBatch();
    my @reqs     = map { \$query->getAclEntry('user', "project: \$pluginName", { systemObjectName => \$_ }) } @objTypes;
    push @reqs, \$query->getProperty('/server/ec_hooks/promote');
    \$query->submit();

    foreach my \$type (@objTypes) {
        if (\$query->findvalue(shift @reqs, 'code') ne 'NoSuchAclEntry') {
            \$batch->deleteAclEntry('user', "project: \$pluginName", { systemObjectName => \$type });
        }
    }

    if (\$promoteAction eq "promote") {
        foreach my \$type (@objTypes) {
            \$batch->createAclEntry(
                                   'user',
                                   "project: \$pluginName",
                                   {
                                      systemObjectName           => \$type,
                                      readPrivilege              => 'allow',
                                      modifyPrivilege            => 'allow',
                                      executePrivilege           => 'allow',
                                      changePermissionsPrivilege => 'allow'
                                   }
                                  );
        }
    }
}

#
# The plugin is being promoted, copy the plugin configurations from the
# earlier version to this instance of the plugin.
#
if (\$upgradeAction eq "upgrade") {
    my \$query   = \$commander->newBatch();
    my \$newcfg  = \$query->getProperty("/plugins/\$pluginName/project/${name}_cfgs");

    my \$old_cfgs_path = "/plugins/\$otherPluginName/project/${name}_cfgs";
    my \$new_cfgs_path = "/plugins/\$pluginName/project/${name}_cfgs";
    my \$oldcfgs = \$query->getProperty(\$old_cfgs_path);
    my \$creds   = \$query->getCredentials("\$[/plugins/\$otherPluginName]");

    local \$self->{abortOnError} = 0;
    \$query->submit();

    # if new plugin does not already have cfgs
    if (\$query->findvalue(\$newcfg, "code") eq "NoSuchProperty") {

        # if old cfg has some cfgs to copy
        if (\$query->findvalue(\$oldcfgs, "code") ne "NoSuchProperty") {
            \$batch->clone(
                          {
                            path      => "/plugins/\$otherPluginName/project/${name}_cfgs",
                            cloneName => "/plugins/\$pluginName/project/${name}_cfgs"
                          }
                         );
        }
    }
}
