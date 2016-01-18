##########################
# sayHello.pl
##########################

use warnings;
use strict;
use Encode;
use utf8;
use open IO => ':encoding(utf8)';
use ElectricCommander;
use ElectricCommander::PropDB;
use Switch;

\$| = 1;

# The configuration parameter
my \$configName = q{\$[config]};

# Create ElectricCommander instance
my \$ec = new ElectricCommander();
\$ec->abortOnError(0);

my \$formalLevel;
# Get the configuration values if the configuration name was specified
my \$cfg  = new ElectricCommander::PropDB( \$ec, "/myProject/${name}_cfgs");
if (\$configName) {
    my %configValues = \$cfg->getRow(\$configName);

    # Check if configuration exists
    unless ( keys(%configValues) ) {
        my \$err = "Configuration '[\$configName]' does not exist. Leave the 'Configuration' empty if you haven't created a configuration yet.\n";
        \$ec->setProperty("summary", \$err);
        print \$err;
        exit 1;
    }

    #Get the formality level that was saved in the configuration.
    \$formalLevel = \$configValues{'formalLevel'};
}

my \$msg = 'Hello World!';
# Change the message based on the formal level selected
switch (\$formalLevel){
   case("Informal") { \$msg = "Hello World!"; }
   case("Surfer") { \$msg = "Yo, World!"; }
   case("Almost Formal") { \$msg = "Greetings World!"; }
}

# Set the message in the job summary as well as print it in the step logs.
\$ec->setProperty("summary", \$msg . "\n");
print \$msg;

exit(0);
