# WikiWalks
Instructions for developers:

Upon cloning the repository, you will need to create 2 files.

The first is "WikiWalksApp\app\src\main\res\values\local.xml"

In this file, paste:

    <?xml version="1.0" encoding="utf-8"?>
    <resources>
        <item name="google_maps_key" type="string">key_goes_here</item>
        <item name="local_url" type="string">server_url_goes_here</item>
    </resources>

And put your Google Maps API key and server URL in their respective locations.

&nbsp;

The second file is "WikiWalksServer\credentials.py"

In this file, paste:

    emailFrom = "your@fromaddress.here"
    emailTo = ["your@recipientaddress.here", "optional@secondrecipient.here"]
    smtpEmail = "your@authenticationaddress.here"
    smtpPassword = "y0ur5m+ppa55w0rd"
    smtpServer = "your.smpt.server.address"
    smtpPort = 465

And replace the examples with your SMTP credentials. This is used for the report function and you don't need to enter real credentials if you are not using it.