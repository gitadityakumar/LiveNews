export const STREAMS = {
  india: [
    'https://ndtv24x7elemarchana.akamaized.net/hls/live/2003678-b/ndtv24x7/master.m3u8',
    'https://dwby15d04agvq.cloudfront.net/index_5.m3u8',
    'https://nw18live.cdn.jio.com/bpk-tv/CNBC_TV18_NW18_MOB/output01/CNBC_TV18_NW18_MOB-audio_98834_eng=98800-video=3724000.m3u8',
    'https://nw18live.cdn.jio.com/bpk-tv/CNN_News18_NW18_MOB/output01/CNN_News18_NW18_MOB-audio_98835_hin=98800-video=3724000.m3u8',
    'https://dztlhgid9me95.cloudfront.net/live-tv/Vidgyor/timesnow/live_720p/chunks.m3u8',
  ],
  usa: [
    'https://dai.google.com/linear/hls/pb/event/crO2xItzS0K0Mznc1p0yPA/stream/715d4599-fce1-4558-a7a3-d3400e9497c8:SIN/master.m3u8',
    'https://dai.google.com/linear/hls/pb/event/BVlOmFGaTi6NpchccR14yA/stream/e7473468-4602-4a26-839c-ccd71f7b4bdd:TPE2/master.m3u8',
    'https://d1ewctnvcwvvvu.cloudfront.net/v1/master/7b67fbda7ab859400a821e9aa0deda20ab7ca3d2/yahooLive/playlist.m3u8?ads.D_ID=%5BD_ID%5D&ads.HS_URL=http%3A%2F%2Fhaystack.tv%2Fid%2F45AsUAVdb&ads.HS_LIVE_AD_TOKEN=3d5ae581763be2fce3d8fb7a1c43eda55d7f87039d086b6c27f90c8fea6194c2d1fee74ceddae92efc18f4cdad9eb75ffe82dbf788f0c5b8045218f89c5153b8&ads.us_privacy=1---&ads.D_MK=web&ads.D_ML=%5BSS_D_ML%5D&ads.D_OV=4.6&ads.D_DNT=%5BD_DNT%5D',
    'https://livenewsplayer.com:1936/cnnsd/cnnsd//playlist.m3u8?dvr&secendtime=1745257480&sechash=mDX9mYnauuv0nbspGU3bBnQYOnMiwb5khpRcQbijK54=&secstarttime=1745246680',
    'https://nw18live.cdn.jio.com/bpk-tv/CNBC_TV18_NW18_MOB/output01/index.m3u8?__hdnea__=st=1744949212~exp=1745035612~acl=/*~hmac=87efaf4076bb149a5eee7a113b35034e2aad785c768264076d9cfb6fb43021f2'
  ],
};

export const NEWS_CHANNELS = {
  india: [
    { id: 1, name: 'NDTV 24x7', category: 'English News', streamIndex: 0 },
    { id: 2, name: 'Z Business', category: 'International', streamIndex: 1 },
    { id: 3, name: 'CNBC TV18', category: 'Business', streamIndex: 2 },
    { id: 4, name: 'CNN-News18', category: 'English News', streamIndex: 3 },
    { id: 5, name: 'Times Now', category: 'English News', streamIndex: 4 },
    { id: 6, name: 'Times Now', category: 'English News', streamIndex: 4 },
   

  ],
  usa: [
    { id: 6, name: 'Bloomberg TV', category: 'Business', streamIndex: 0 },
    { id: 7, name: 'ABC News Live', category: 'US News', streamIndex: 1 },
    { id: 8, name: 'Yahoo Finance', category: 'Finance', streamIndex: 2 },
    { id: 9, name: 'CNN Live', category: 'US News', streamIndex: 3 },
    { id: 10, name: 'CNBC US', category: 'Business', streamIndex: 4 },
  ],
};