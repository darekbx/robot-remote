from multiprocessing.connection import Listener

address = ('localhost', 6000)
listener = Listener(address)
connection = listener.accept()

print('connection accepted from', listener.last_accepted)

while True:
    command = connection.recv()
    print("Received command {0}".format(command))
    if command == 'close':
        connection.close()
        break
listener.close()