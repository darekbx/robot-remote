from multiprocessing.connection import Client
import sys

if len(sys.argv) > 0:
    command = sys.argv[1]
    address = ('localhost', 6000)
    connection = Client(address)
    connection.send(command)
    connection.close()