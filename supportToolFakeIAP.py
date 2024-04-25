from http.server import BaseHTTPRequestHandler, HTTPServer
import time
import requests

hostName = "localhost"
serverPort = 8080

super_user = "bob@bob.com"


class MyServer(BaseHTTPRequestHandler):
    def do_GET(self):
        self.send_response(200)

        response = requests.get("http://127.0.0.1:9999" + self.path,
                                headers={"x-goog-iap-jwt-assertion": super_user})

        self.send_header("Content-type", "text/html")
        self.end_headers()
        self.wfile.write(bytes(response.text, encoding='utf8'))

    def do_POST(self):
        self.send_response(200)
        self.send_header("Content-type", "text/html")
        self.end_headers()
        self.wfile.write(bytes("<html><head><title>https://pythonbasics.org</title></head>", "utf-8"))
        self.wfile.write(bytes("<p>Request: %s</p>" % self.path, "utf-8"))
        self.wfile.write(bytes("<body>", "utf-8"))
        self.wfile.write(bytes("<p>NOT IMPLEMENTED.</p>", "utf-8"))
        self.wfile.write(bytes("</body></html>", "utf-8"))


if __name__ == "__main__":
    webServer = HTTPServer((hostName, serverPort), MyServer)
    print("Server started http://%s:%s" % (hostName, serverPort))

    try:
        webServer.serve_forever()
    except KeyboardInterrupt:
        pass

    webServer.server_close()
    print("Server stopped.")
