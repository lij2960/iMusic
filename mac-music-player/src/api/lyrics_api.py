"""在线歌词API（与Android版本一致）"""
import requests
from typing import Optional, List, Tuple


class LyricsAPI:
    """在线歌词搜索API"""
    
    # 与Android版本使用相同的API
    SEARCH_API = "https://music.163.com/api/search/get/web"
    LYRICS_API = "https://music.163.com/api/song/lyric"
    
    @staticmethod
    def search_online_lyrics(title: str, artist: str) -> Optional[str]:
        """搜索在线歌词（与Android版本逻辑一致）"""
        try:
            keywords = f"{title} {artist}"
            print(f"API call: searching music for lyrics: {keywords}")
            
            # 搜索歌曲
            search_params = {
                's': keywords,
                'type': 1,
                'limit': 10,
                'offset': 0
            }
            
            headers = {
                'User-Agent': 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36',
                'Referer': 'https://music.163.com/'
            }
            
            search_response = requests.get(
                LyricsAPI.SEARCH_API,
                params=search_params,
                headers=headers,
                timeout=10
            )
            
            print(f"Search API response code: {search_response.status_code}")
            
            if search_response.status_code == 200:
                search_data = search_response.json()
                songs = search_data.get('result', {}).get('songs', [])
                
                print(f"Found {len(songs)} songs")
                
                if songs:
                    first_song = songs[0]
                    song_id = first_song.get('id')
                    
                    print(f"Found song ID: {song_id}, getting lyrics...")
                    
                    # 获取歌词
                    lyrics_params = {
                        'id': song_id,
                        'lv': -1,
                        'tv': -1
                    }
                    
                    lyrics_response = requests.get(
                        LyricsAPI.LYRICS_API,
                        params=lyrics_params,
                        headers=headers,
                        timeout=10
                    )
                    
                    print(f"Lyrics API response code: {lyrics_response.status_code}")
                    
                    if lyrics_response.status_code == 200:
                        lyrics_data = lyrics_response.json()
                        lyrics = lyrics_data.get('lrc', {}).get('lyric')
                        
                        if lyrics:
                            print(f"Lyrics found: {lyrics[:100]}...")
                            return lyrics
                        else:
                            print("No lyrics in response")
                            return None
                    else:
                        print(f"Lyrics API error: {lyrics_response.status_code}")
                        return None
                else:
                    print("No songs found in search")
                    return None
            else:
                print(f"Search API error: {search_response.status_code}")
                return None
        
        except Exception as e:
            print(f"Exception in search_online_lyrics: {e}")
            import traceback
            traceback.print_exc()
            return None
    
    @staticmethod
    def search_multiple_lyrics(title: str, artist: str) -> List[Tuple[str, str]]:
        """搜索多个歌词选项（与Android版本逻辑一致）"""
        try:
            keywords = f"{title} {artist}"
            print(f"Searching multiple lyrics for: {keywords}")
            
            # 搜索歌曲
            search_params = {
                's': keywords,
                'type': 1,
                'limit': 10,
                'offset': 0
            }
            
            headers = {
                'User-Agent': 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36',
                'Referer': 'https://music.163.com/'
            }
            
            search_response = requests.get(
                LyricsAPI.SEARCH_API,
                params=search_params,
                headers=headers,
                timeout=10
            )
            
            if search_response.status_code == 200:
                search_data = search_response.json()
                songs = search_data.get('result', {}).get('songs', [])
                
                lyrics_options = []
                
                # 获取前5首歌曲的歌词
                for song in songs[:5]:
                    try:
                        song_id = song.get('id')
                        song_name = song.get('name', 'Unknown')
                        artists = song.get('artists', [])
                        artist_name = artists[0].get('name', 'Unknown') if artists else 'Unknown'
                        
                        # 获取歌词
                        lyrics_params = {
                            'id': song_id,
                            'lv': -1,
                            'tv': -1
                        }
                        
                        lyrics_response = requests.get(
                            LyricsAPI.LYRICS_API,
                            params=lyrics_params,
                            headers=headers,
                            timeout=10
                        )
                        
                        if lyrics_response.status_code == 200:
                            lyrics_data = lyrics_response.json()
                            lyrics = lyrics_data.get('lrc', {}).get('lyric')
                            
                            if lyrics and lyrics.strip():
                                display_name = f"{song_name} - {artist_name}"
                                lyrics_options.append((display_name, lyrics))
                    
                    except Exception as e:
                        print(f"Error getting lyrics for song {song_id}: {e}")
                        continue
                
                return lyrics_options
            
            return []
        
        except Exception as e:
            print(f"Exception in search_multiple_lyrics: {e}")
            return []