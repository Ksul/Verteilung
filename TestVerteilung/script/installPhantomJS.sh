export PHANTOM_JS="phantomjs-2.1.1-linux-x86_64"
cd ~
wget https://bitbucket.org/ariya/phantomjs/downloads/$PHANTOM_JS.tar.bz2
tar xvjf $PHANTOM_JS.tar.bz2
sudo mv $PHANTOM_JS  /usr/local/share/
sudo ln -s /usr/local/share/$PHANTOM_JS/bin/phantomjs /usr/local/share/phantomjs
sudo ln -s /usr/local/share/$PHANTOM_JS/bin/phantomjs /usr/local/bin/phantomjs
## system wide
sudo ln -s /usr/local/share/$PHANTOM_JS/bin/phantomjs /usr/bin/phantomjs

phantomjs -v