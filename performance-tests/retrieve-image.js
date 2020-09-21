import http from 'k6/http';
import {check} from 'k6';

export let options = {
  vus: 60,
  duration: "30s"
};

export default function () {
  let res = http.get('http://localhost:10001/storage/retrieve/1-6d1ce2361f46a7198f3de7e50df7f0b934ea812245e842fbf437f379d515485d.jpg');

  check(res, {
    'is status 200': (r) => r.status === 200
  })
}
