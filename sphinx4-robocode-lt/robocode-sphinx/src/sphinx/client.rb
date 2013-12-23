require 'socket'      # Sockets are in standard library

hostname = '127.0.0.1'
port = 9090

s = TCPSocket.open(hostname, port)

while line = s.gets   # Read lines from the socket
  puts line.chop      # And print with platform line terminator
end
s.close               # Close the socket when don