import http from 'k6/http';
import {check} from 'k6';

let binFile = open('./testphoto.jpg', 'b');

export let options = {
  vus: 30,
  duration: "10s"
};

export default function () {
  let data = {
    file: http.file(binFile, "test.bin")
  };

  let res = http.post('http://localhost:10001/storage/upload', data);

  check(res, {
    'is status 200': (r) => r.status === 200
  })
}
