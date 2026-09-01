import { FormEvent, useEffect, useMemo, useState } from "react";
import {
  Bell, Bookmark, Check, ChevronLeft, Compass, Edit3, FileImage, Heart,
  Home, LogIn, LogOut, Menu, MessageCircle, MoreHorizontal, Plus, Search,
  Send, Settings, Shield, Sparkles, Trash2, UserRound, Users, X, EyeOff,
  Upload, ImagePlus
} from "lucide-react";
import {
  api, decodeToken, getKnownChannelIds, rememberChannel, forgetChannel,
  restoreSession, session, type Channel, type Comment, type FeedItem, type Media, type Notification,
  type Post, type Subscription, type TokenPayload, type UserResponse
} from "./api";

// The UI never asks a user to type UUIDs. UUIDs are internal API values only.
type View = "home" | "channels" | "following" | "notifications" | "profile" | "explore" | "moderation";

export default function App() {
  const [view, setView] = useState<View>("home");
  const [mobileOpen, setMobileOpen] = useState(false);
  const [logged, setLogged] = useState(false);
  const [authReady, setAuthReady] = useState(false);
  const [authOpen, setAuthOpen] = useState(false);
  const [authMode, setAuthMode] = useState<"login" | "register">("login");
  const [composerOpen, setComposerOpen] = useState(false);
  const [query, setQuery] = useState("");
  const [error, setError] = useState("");
  const [tick, setTick] = useState(0);

  const user = logged ? session.getUser() : null;

  useEffect(() => {
    let cancelled = false;

    const handleExpired = () => {
      session.clear();
      setLogged(false);
      setView("home");
      setAuthMode("login");
      setAuthOpen(true);
    };

    window.addEventListener("postly:session-expired", handleExpired);

    restoreSession()
      .then(ok => {
        if (!cancelled) setLogged(ok);
      })
      .finally(() => {
        if (!cancelled) setAuthReady(true);
      });

    return () => {
      cancelled = true;
      window.removeEventListener("postly:session-expired", handleExpired);
    };
  }, []);

  const refreshUi = () => setTick(v => v + 1);
  const go = (next: View) => { setView(next); setMobileOpen(false); };
  const requireAuth = (action: () => void) => {
    if (!session.isLoggedIn()) {
      setAuthMode("login");
      setAuthOpen(true);
      return;
    }
    action();
  };

  const logout = () => {
    session.clear();
    setLogged(false);
    setView("home");
  };

  if (!authReady) {
    return <div className="app"><section className="guest"><div className="guest-icon"><Sparkles/></div><small>POSTLY</small><h1>Загрузка…</h1><p>Восстанавливаем вашу сессию.</p></section></div>;
  }

  return (
    <div className="app">
      <header className="topbar">
        <button className="mobile-menu" onClick={() => setMobileOpen(v => !v)}><Menu /></button>
        <button className="brand" onClick={() => go("home")}><span className="brand-mark"><Sparkles size={17}/></span><span>Postly</span></button>
        <div className="search"><Search size={17}/><input value={query} onChange={e => setQuery(e.target.value)} placeholder="Поиск по ленте и каналам..."/>{query && <button onClick={() => setQuery("")}><X size={15}/></button>}</div>
        <div className="top-actions">
          {logged && user?.userId && <button className="notification-button" onClick={() => go("notifications")}><Bell size={19}/><UnreadBadge userId={user.userId} tick={tick}/></button>}
          {logged ? <button className="avatar" onClick={() => go("profile")}>{initials(user?.sub)}</button> : <button className="login-button" onClick={() => {setAuthMode("login");setAuthOpen(true)}}><LogIn size={16}/> Войти</button>}
        </div>
      </header>

      <div className="shell">
        <aside className={`sidebar ${mobileOpen ? "open" : ""}`}>
          <nav>
            <NavItem icon={<Home/>} label="Главная" active={view === "home"} onClick={() => go("home")}/>
            <NavItem icon={<Compass/>} label="Обзор" active={view === "explore"} onClick={() => go("explore")}/>
            <NavItem icon={<Users/>} label="Каналы" active={view === "channels"} onClick={() => go("channels")}/>
            {logged && <NavItem icon={<Bookmark/>} label="Мои подписки" active={view === "following"} onClick={() => go("following")}/>} 
            {logged && <NavItem icon={<Bell/>} label="Уведомления" active={view === "notifications"} onClick={() => go("notifications")}/>}
            {logged && <NavItem icon={<Shield/>} label="Модерация" active={view === "moderation"} onClick={() => go("moderation")}/>}
            {logged && <NavItem icon={<UserRound/>} label="Профиль" active={view === "profile"} onClick={() => go("profile")}/>} 
          </nav>
          <div className="sidebar-bottom">
            <button className="create-post" onClick={() => requireAuth(() => setComposerOpen(true))}><Plus size={17}/> Создать пост</button>
            {logged && <button className="logout-side" onClick={logout}><LogOut size={16}/> Выйти</button>}
          </div>
        </aside>

        <main className="content">
          {error && <div className="error-banner">{error}<button onClick={() => setError("")}><X size={15}/></button></div>}
          {view === "home" && <HomeView query={query} tick={tick} logged={logged} onError={setError} onCreate={() => requireAuth(() => setComposerOpen(true))} onRefresh={refreshUi} />}
          {view === "explore" && <ExploreView query={query} tick={tick} onError={setError} onRefresh={refreshUi} />}
          {view === "channels" && <ChannelsView logged={logged} tick={tick} onError={setError} onRefresh={refreshUi} />}
          {view === "following" && logged && <FollowingView tick={tick} onError={setError} onRefresh={refreshUi} />}
          {view === "notifications" && logged && user?.userId && <NotificationsView userId={user.userId} tick={tick} onError={setError} onRefresh={refreshUi}/>}
          {view === "moderation" && logged && <ModerationView tick={tick} onError={setError} onRefresh={refreshUi}/>}
          {view === "profile" && logged && user?.userId && <ProfileView userId={user.userId} username={user.sub || "Пользователь"} tick={tick} onError={setError} onRefresh={refreshUi}/>} 
        </main>
      </div>

      {composerOpen && <CreatePostModal onClose={() => setComposerOpen(false)} onCreated={() => {setComposerOpen(false);refreshUi()}} onError={setError}/>} 
      {authOpen && <AuthModal mode={authMode} setMode={setAuthMode} onClose={() => setAuthOpen(false)} onSuccess={() => {setLogged(true);setAuthOpen(false);refreshUi()}} onError={setError}/>} 
    </div>
  );
}

function HomeView({query,tick,logged,onError,onCreate,onRefresh}:{query:string;tick:number;logged:boolean;onError:(x:string)=>void;onCreate:()=>void;onRefresh:()=>void}) {
  const [items,setItems] = useState<FeedItem[]>([]); const [page,setPage] = useState(0); const [loading,setLoading]=useState(false);
  useEffect(()=>{ if(!logged){setItems([]);return;} setLoading(true); api.feed(page,20).then(r=>setItems(r.content||[])).catch(e=>onError(e.message)).finally(()=>setLoading(false)); },[logged,page,tick]);
  const shown = useMemo(()=>items.filter(x=>!query || `${x.title} ${x.content}`.toLowerCase().includes(query.toLowerCase())),[items,query]);
  if(!logged) return <GuestHome onLogin={onCreate}/>;
  return <>
    <section className="hero"><div><small>ЛИЧНАЯ ЛЕНТА</small><h1>Идеи, которыми<br/><em>хочется делиться.</em></h1><p>Здесь появляются опубликованные посты из каналов, на которые вы подписаны.</p></div><div className="hero-mark"><Sparkles size={39}/></div></section>
    <div className="section-head"><div><h2>Лента</h2><small>{loading?"Загрузка…":`${shown.length} публикаций`}</small></div><button className="accent-button" onClick={onCreate}><Plus size={16}/> Создать пост</button></div>
    <div className="feed-list">{shown.map((x,i)=><FeedCard key={x.postId} item={x} index={i} onError={onError} onRefresh={onRefresh}/>)}{!loading&&!shown.length&&<Empty title="В ленте пока пусто" text="Подпишитесь на канал или создайте свою первую публикацию."/>}</div>
    <div className="pagination"><button disabled={page===0} onClick={()=>setPage(v=>Math.max(0,v-1))}><ChevronLeft size={15}/> Назад</button><span>Страница {page+1}</span><button disabled={items.length<20} onClick={()=>setPage(v=>v+1)}>Вперёд</button></div>
  </>;
}

function GuestHome({onLogin}:{onLogin:()=>void}) { return <section className="guest"><div className="guest-icon"><Sparkles/></div><small>POSTLY</small><h1>Добро пожаловать.</h1><p>Смотрите ленту, подписывайтесь на каналы и общайтесь с другими пользователями.</p><button className="accent-button" onClick={onLogin}><LogIn size={16}/> Войти в аккаунт</button></section>; }

function ExploreView({query,tick,onError,onRefresh}:{query:string;tick:number;onError:(x:string)=>void;onRefresh:()=>void}) {
  const [channels,setChannels]=useState<Channel[]>([]); const [posts,setPosts]=useState<Post[]>([]); const [loading,setLoading]=useState(false);
  useEffect(()=>{loadKnownChannels(onError).then(cs=>{setChannels(cs);if(!cs.length){setPosts([]);return;}setLoading(true);return Promise.all(cs.map(c=>api.getChannelPosts(c.id).catch(()=>[]))).then(groups=>setPosts(groups.flat())).finally(()=>setLoading(false));})},[tick]);
  const shown=posts.filter(p=>!query||`${p.title} ${p.content}`.toLowerCase().includes(query.toLowerCase()));
  return <><PageTitle eyebrow="ОБЗОР" title="Обзор" text="Публикации из ваших известных каналов. UUID вам не нужны — приложение получает их само через подписки и созданные каналы."/>
    <div className="mini-stats"><div><strong>{channels.length}</strong><span>каналов</span></div><div><strong>{posts.length}</strong><span>постов</span></div></div>
    <div className="feed-list">{loading&&<div className="loading">Загрузка публикаций…</div>}{shown.map((p,i)=><PostCard key={p.id} post={p} index={i} onError={onError} onRefresh={onRefresh}/>)}{!loading&&!shown.length&&<Empty title="Нечего показывать" text="Подпишитесь на канал или создайте канал, чтобы здесь появились публикации."/>}</div></>;
}

function ChannelsView({logged,tick,onError,onRefresh}:{logged:boolean;tick:number;onError:(x:string)=>void;onRefresh:()=>void}) {
  const [channels,setChannels]=useState<Channel[]>([]); const [createOpen,setCreateOpen]=useState(false); const [selected,setSelected]=useState<Channel|null>(null);
  useEffect(()=>{loadKnownChannels(onError).then(setChannels)},[logged,tick]);
  const remove=async(id:string)=>{try{await api.deleteChannel(id);forgetChannel(id);setSelected(null);onRefresh()}catch(e){onError(e instanceof Error?e.message:"Не удалось удалить канал")}};
  return <><div className="page-title-row"><PageTitle eyebrow="СООБЩЕСТВА" title="Каналы" text="Список формируется автоматически. Никаких UUID — только названия каналов и действия пользователя."/><button className="accent-button" onClick={()=>{if(!logged){onError("Сначала войдите в аккаунт");return}setCreateOpen(true)}}><Plus size={16}/> Создать канал</button></div>
    <div className="channel-grid">{channels.map(c=><ChannelCard key={c.id} channel={c} logged={logged} tick={tick} onOpen={()=>setSelected(c)} onError={onError} onRefresh={onRefresh}/>)}</div>{!channels.length&&<Empty title="Каналов пока нет" text="Создайте свой первый канал или подпишитесь на канал через социальные действия."/>}
    {createOpen&&<ChannelModal title="Создать канал" onClose={()=>setCreateOpen(false)} onSave={async(name,description)=>{try{const c=await api.createChannel({name,description});rememberChannel(c.id);setCreateOpen(false);onRefresh()}catch(e){onError(e instanceof Error?e.message:"Не удалось создать канал")}}}/>} 
    {selected&&<ChannelDetails channel={selected} onClose={()=>setSelected(null)} onError={onError} onRefresh={onRefresh}/>} 
  </>;
}

function ChannelCard({channel,logged,tick,onOpen,onError,onRefresh}:{channel:Channel;logged:boolean;tick:number;onOpen:()=>void;onError:(x:string)=>void;onRefresh:()=>void}) { const [following,setFollowing]=useState(false); useEffect(()=>{if(!logged){setFollowing(false);return;}api.mySubscriptions().then(xs=>setFollowing(xs.some(x=>x.channelId===channel.id))).catch(()=>{})},[logged,channel.id,tick]); const toggle=async()=>{try{if(following){await api.unsubscribe(channel.id);setFollowing(false)}else{await api.subscribe(channel.id);setFollowing(true)}onRefresh()}catch(e){onError(e instanceof Error?e.message:"Не удалось изменить подписку")}}; return <div className="channel-card"><div className="channel-symbol">{channel.name.slice(0,1).toUpperCase()}</div><div className="channel-main"><h3>{channel.name}</h3><p>{channel.description||"Без описания"}</p></div><button className="ghost-button" onClick={onOpen}>Открыть</button>{logged&&<button className="follow-button" onClick={toggle}>{following?"Вы подписаны":"Подписаться"}</button>}</div>; }

function ChannelDetails({channel,onClose,onError,onRefresh}:{channel:Channel;onClose:()=>void;onError:(x:string)=>void;onRefresh:()=>void}) { const [posts,setPosts]=useState<Post[]>([]); const [edit,setEdit]=useState(false); const [name,setName]=useState(channel.name); const [description,setDescription]=useState(channel.description||""); const [token]=useState<TokenPayload|null>(session.getUser()); useEffect(()=>{api.getChannelPosts(channel.id).then(setPosts).catch(e=>onError(e.message))},[channel.id]); const own=token?.userId===channel.ownerId; const save=async()=>{try{await api.updateChannel(channel.id,{name,description});setEdit(false);onRefresh()}catch(e){onError(e instanceof Error?e.message:"Не удалось изменить канал")}};return <Modal close={onClose}><div className="modal-head"><div><small>КАНАЛ</small><h2>{channel.name}</h2></div><button onClick={onClose}><X/></button></div><p className="modal-subtitle">{channel.description||"Без описания"}</p><div className="channel-posts-mini"><strong>Публикации</strong><span>{posts.length}</span></div>{posts.slice(0,6).map(p=><div className="mini-post" key={p.id}><strong>{p.title}</strong><span>{p.content}</span></div>)}{own&&<div className="modal-actions"><button className="ghost-button" onClick={()=>setEdit(true)}><Edit3 size={15}/> Редактировать</button><button className="danger-button" onClick={async()=>{if(confirm("Удалить канал?")){try{await api.deleteChannel(channel.id);forgetChannel(channel.id);onClose();onRefresh()}catch(e){onError(e instanceof Error?e.message:"Не удалось удалить канал")}}}}><Trash2 size={15}/> Удалить</button></div>}{edit&&<div className="edit-panel"><label>Название<input value={name} onChange={e=>setName(e.target.value)}/></label><label>Описание<textarea value={description} onChange={e=>setDescription(e.target.value)}/></label><button className="accent-button" onClick={save}>Сохранить</button></div>}</Modal>; }

function FollowingView({tick,onError,onRefresh}:{tick:number;onError:(x:string)=>void;onRefresh:()=>void}) {
  const [subs,setSubs]=useState<Subscription[]>([]);
  const [channels,setChannels]=useState<Channel[]>([]);
  const [selected,setSelected]=useState<Channel|null>(null);

  useEffect(()=>{
    api.mySubscriptions()
      .then(async xs=>{
        setSubs(xs);
        const cs=await Promise.all(xs.map(x=>api.getChannel(x.channelId).catch(()=>null)));
        setChannels(cs.filter(Boolean) as Channel[]);
      })
      .catch(e=>onError(e.message));
  },[tick]);

  return <>
    <PageTitle
      eyebrow="МОИ СООБЩЕСТВА"
      title="Подписки"
      text="Управляйте каналами, на которые вы подписаны."
    />

    <div className="channel-grid">
      {channels.map(c=>
        <ChannelCard
          key={c.id}
          channel={c}
          logged
          tick={tick}
          onOpen={()=>setSelected(c)}
          onError={onError}
          onRefresh={onRefresh}
        />
      )}
    </div>

    {!channels.length&&
      <Empty
        title="Нет подписок"
        text="Подпишитесь на канал в разделе «Каналы»."
      />
    }

    <div className="hidden-count">{subs.length} активных подписок</div>

    {selected&&
      <ChannelDetails
        channel={selected}
        onClose={()=>setSelected(null)}
        onError={onError}
        onRefresh={onRefresh}
      />
    }
  </>;
}

function FeedCard({item,index,onError,onRefresh}:{item:FeedItem;index:number;onError:(x:string)=>void;onRefresh:()=>void}) { const post:Post={id:item.postId,channelId:item.channelId,authorId:item.authorId,title:item.title,content:item.content,status:"PUBLISHED",createdAt:item.publishedAt,updatedAt:item.publishedAt,publishedAt:item.publishedAt}; return <PostCard post={post} index={index} onError={onError} onRefresh={onRefresh}/>; }

const channelCache = new Map<string, Channel>();
const userCache = new Map<string, UserResponse>();

function PostCard({post,index,onError,onRefresh}:{post:Post;index:number;onError:(x:string)=>void;onRefresh:()=>void}) {
  const [likes,setLikes]=useState(0);
  const [liked,setLiked]=useState(false);
  const [comments,setComments]=useState<Comment[]>([]);

  const likeStorageKey = `postly.liked.${session.getUser()?.userId ?? "anonymous"}.${post.id}`;
  const [showComments,setShowComments]=useState(false);
  const [comment,setComment]=useState("");
  const [images,setImages]=useState<Media[]>([]);
  const [channel,setChannel]=useState<Channel|null>(channelCache.get(post.channelId)||null);
  const [author,setAuthor]=useState<UserResponse|null>(userCache.get(post.authorId)||null);

  const me=session.getUser();
  const mine=me?.userId===post.authorId;

  useEffect(()=>{
    let cancelled=false;

    api.likeCount(post.id).then(setLikes).catch(()=>{});
    api.getImages(post.id).then(setImages).catch(()=>{});

    // Сохраняем состояние лайка локально, чтобы после перезагрузки
    // кнопка знала, что пользователь уже лайкнул этот пост.
    try {
      setLiked(localStorage.getItem(likeStorageKey) === "1");
    } catch {
      setLiked(false);
    }

    const loadAuthor=async()=>{
      if(userCache.has(post.authorId)){
        if(!cancelled) setAuthor(userCache.get(post.authorId)!);
        return;
      }

      try{
        const value=await api.user(post.authorId);
        userCache.set(post.authorId,value);
        if(!cancelled) setAuthor(value);
      }catch{
        // Имя пользователя необязательно для отображения поста.
      }
    };

    const loadChannel=async()=>{
      if(channelCache.has(post.channelId)){
        if(!cancelled) setChannel(channelCache.get(post.channelId)!);
        return;
      }

      try{
        const value=await api.getChannel(post.channelId);
        channelCache.set(post.channelId,value);
        if(!cancelled) setChannel(value);
      }catch{
        // Название канала необязательно для отображения поста.
      }
    };

    void loadAuthor();
    void loadChannel();

    return ()=>{cancelled=true};
  },[post.id,post.authorId,post.channelId]);

  const toggleLike=async()=>{
    try{
      if(liked){
        await api.unlike(post.id);
        setLiked(false);
        setLikes(v=>Math.max(0,v-1));
        try { localStorage.removeItem(likeStorageKey); } catch {}
        return;
      }

      try {
        await api.like(post.id);
        setLiked(true);
        setLikes(v=>v+1);
        try { localStorage.setItem(likeStorageKey, "1"); } catch {}
      } catch (likeError) {
        // После перезагрузки liked=false, хотя лайк уже существует в БД.
        // Повторный POST /like тогда возвращает ошибку "already liked".
        // В таком случае трактуем нажатие как снятие существующего лайка.
        try {
          await api.unlike(post.id);
          setLiked(false);
          try { localStorage.removeItem(likeStorageKey); } catch {}
          const actualCount = await api.likeCount(post.id).catch(() => null);
          if (typeof actualCount === "number") {
            setLikes(actualCount);
          } else {
            setLikes(v=>Math.max(0,v-1));
          }
        } catch {
          throw likeError;
        }
      }
    }catch(e){
      onError(e instanceof Error?e.message:"Не удалось изменить лайк");
    }
  };

  const toggleComments=async()=>{
    if(!showComments){
      try{
        setComments(await api.comments(post.id));
      }catch(e){
        onError(e instanceof Error?e.message:"Не удалось загрузить комментарии");
      }
    }
    setShowComments(v=>!v);
  };

  const addComment=async()=>{
    if(!comment.trim()) return;
    try{
      const c=await api.createComment(post.id,comment);
      setComments(v=>[...v,c]);
      setComment("");
    }catch(e){
      onError(e instanceof Error?e.message:"Не удалось добавить комментарий");
    }
  };

  const displayName=channel?.name || author?.username || "Пользователь";
  const secondaryName=channel?.name && author?.username
    ? `@${author.username}`
    : author?.username
      ? `@${author.username}`
      : "Канал";

  return <article className="post-card" style={{animationDelay:`${index*35}ms`}}>
    <div className="post-header">
      <div className="post-avatar">{initials(channel?.name || author?.username || post.authorId)}</div>
      <div>
        <strong>{displayName}</strong>
        <small>{secondaryName} · {timeAgo(post.createdAt)} · {statusRu(normalizePostStatus(post.status))}</small>
      </div>
      <span className={`status ${normalizePostStatus(post.status).toLowerCase()}`}>
        {statusRu(normalizePostStatus(post.status))}
      </span>
      <button className="more"><MoreHorizontal size={18}/></button>
    </div>

    <h3>{post.title}</h3>
    <p>{post.content}</p>

    {images.length>0&&
      <div className="media-grid">
        {images.map(m=><img key={m.id} src={m.url} alt={m.originalName}/>)}
      </div>
    }

    <div className="post-actions">
      <button className={liked?"liked":""} onClick={toggleLike}>
        <Heart size={18} fill={liked?"currentColor":"none"}/>{likes}
      </button>
      <button onClick={toggleComments}>
        <MessageCircle size={18}/>{comments.length||""}
      </button>
      <button className="share"><Send size={16}/> Поделиться</button>
      {mine&&<PostActions post={post} onError={onError} onRefresh={onRefresh}/>}
    </div>

    {showComments&&
      <div className="comments">
        {comments.map(c=>
          <div className="comment" key={c.id}>
            <div className="comment-avatar">{initials(c.userId)}</div>
            <div>
              <strong>Пользователь</strong>
              <span>{c.content}</span>
              <small>{timeAgo(c.createdAt)}</small>
            </div>
          </div>
        )}
        <div className="comment-input">
          <input
            value={comment}
            onChange={e=>setComment(e.target.value)}
            onKeyDown={e=>e.key==="Enter"&&addComment()}
            placeholder="Напишите комментарий..."
          />
          <button onClick={addComment}><Send size={15}/></button>
        </div>
      </div>
    }
  </article>;
}

function PostActions({post,onError,onRefresh}:{post:Post;onError:(x:string)=>void;onRefresh:()=>void}) {
  const run = async (action:()=>Promise<any>) => {
    try {
      await action();
      onRefresh();
    } catch (e) {
      onError(e instanceof Error ? e.message : "Операция не выполнена");
    }
  };

  const status = normalizePostStatus(post.status);

  return (
    <div className="owner-actions">
      {(status === "DRAFT" || status === "REJECTED") && (
        <button
          title="Отправить на модерацию"
          onClick={() => void run(() => api.moderatePost(post.id))}
        >
          <Shield size={15}/>
        </button>
      )}

      {status === "PUBLISHED" && (
        <button
          title="Скрыть"
          onClick={() => void run(() => api.hidePost(post.id))}
        >
          <EyeOff size={15}/>
        </button>
      )}

      {status !== "DELETED" && (
        <button
          title="Удалить"
          onClick={() => {
            if (confirm("Удалить пост?")) {
              void run(() => api.deletePost(post.id));
            }
          }}
        >
          <Trash2 size={15}/>
        </button>
      )}

      {status !== "DELETED" && (
        <label className="upload-button" title="Добавить изображение">
          <ImagePlus size={15}/>
          <input
            type="file"
            accept="image/*"
            onChange={e => {
              const f = e.target.files?.[0];
              if (f) void run(() => api.uploadImage(post.id, f));
            }}
            hidden
          />
        </label>
      )}
    </div>
  );
}

function ModerationView({
  tick,
  onError,
  onRefresh
}: {
  tick:number;
  onError:(x:string)=>void;
  onRefresh:()=>void;
}) {
  const [posts,setPosts] = useState<Post[]>([]);
  const [loading,setLoading] = useState(false);
  const [busyId,setBusyId] = useState<string|null>(null);

  const load = async () => {
    setLoading(true);

    try {
      // Берём посты из каналов, которые известны фронтенду.
      // Backend endpoint /posts/channel/{id} в некоторых версиях
      // возвращает только опубликованные посты, поэтому одних его
      // данных недостаточно для очереди модерации.
      const channels = await loadKnownChannels(onError);

      const groups = await Promise.all(
        channels.map(async channel => {
          try {
            return await api.getChannelPosts(channel.id);
          } catch {
            return [];
          }
        })
      );

      const fromApi = groups.flat();

      // Пост, только что отправленный на модерацию, сохраняем на фронте.
      // Это позволяет показать его сразу, даже если backend список канала
      // пока отдаёт только PUBLISHED.
      const localPending = getPendingModerationPosts();

      const merged = [...fromApi, ...localPending];
      const unique = new Map<string, Post>();

      merged.forEach(post => {
        if (normalizePostStatus(post.status) === "PENDING_MODERATION") {
          unique.set(post.id, {
            ...post,
            status: "PENDING_MODERATION"
          });
        }
      });

      const pending = [...unique.values()].sort(
        (a,b) =>
          new Date(b.createdAt).getTime() -
          new Date(a.createdAt).getTime()
      );

      setPosts(pending);
    } catch (e) {
      onError(
        e instanceof Error
          ? e.message
          : "Не удалось загрузить посты на модерацию"
      );
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void load();
  }, [tick]);

  const approve = async (post:Post) => {
    setBusyId(post.id);

    try {
      // ВАЖНО:
      // PENDING_MODERATION -> PUBLISHED
      // Здесь нельзя вызывать moderatePost(), потому что он делает
      // DRAFT -> PENDING_MODERATION.
      await api.publishPost(post.id);

      forgetPendingModerationPost(post.id);
      setPosts(current => current.filter(x => x.id !== post.id));
      onRefresh();
    } catch (e) {
      onError(
        e instanceof Error
          ? e.message
          : "Не удалось опубликовать пост"
      );
    } finally {
      setBusyId(null);
    }
  };

  const hide = async (post:Post) => {
    setBusyId(post.id);

    try {
      await api.hidePost(post.id);

      forgetPendingModerationPost(post.id);
      setPosts(current => current.filter(x => x.id !== post.id));
      onRefresh();
    } catch (e) {
      onError(
        e instanceof Error
          ? e.message
          : "Не удалось скрыть пост"
      );
    } finally {
      setBusyId(null);
    }
  };

  const remove = async (post:Post) => {
    if (!confirm("Удалить этот пост?")) return;

    setBusyId(post.id);

    try {
      await api.deletePost(post.id);

      forgetPendingModerationPost(post.id);
      setPosts(current => current.filter(x => x.id !== post.id));
      onRefresh();
    } catch (e) {
      onError(
        e instanceof Error
          ? e.message
          : "Не удалось удалить пост"
      );
    } finally {
      setBusyId(null);
    }
  };

  return (
    <>
      <PageTitle
        eyebrow="МОДЕРАЦИЯ"
        title="Посты на модерации"
        text="Здесь отображаются только посты, которые были отправлены на модерацию."
      />

      <div className="section-head">
        <div>
          <h2>Ожидают проверки</h2>
          <small>
            {loading ? "Загрузка…" : `${posts.length} постов`}
          </small>
        </div>
      </div>

      <div className="feed-list">
        {posts.map((post,index) => (
          <article
            className="post-card"
            key={post.id}
            style={{animationDelay:`${index * 35}ms`}}
          >
            <div className="post-header">
              <div className="post-avatar">{initials(post.authorId)}</div>

              <div>
                <strong>Пользователь</strong>
                <small>
                  {timeAgo(post.createdAt)} · На модерации
                </small>
              </div>

              <span className="status pending_moderation">
                На модерации
              </span>
            </div>

            <h3>{post.title}</h3>
            <p>{post.content}</p>

            <ModerationImages postId={post.id} />

            <div className="moderation-actions">
              <button
                className="accent-button"
                disabled={busyId === post.id}
                onClick={() => void approve(post)}
              >
                <Check size={15}/>
                {busyId === post.id ? "Обработка…" : "Опубликовать"}
              </button>

              <button
                className="ghost-button"
                disabled={busyId === post.id}
                onClick={() => void hide(post)}
              >
                <EyeOff size={15}/>
                Скрыть
              </button>

              <button
                className="danger-button"
                disabled={busyId === post.id}
                onClick={() => void remove(post)}
              >
                <Trash2 size={15}/>
                Удалить
              </button>
            </div>
          </article>
        ))}

        {!loading && !posts.length && (
          <Empty
            title="Постов на модерации нет"
            text="Когда вы создадите пост и отправите его на модерацию, он появится здесь."
          />
        )}

        {loading && (
          <div className="loading">
            Загрузка постов на модерацию…
          </div>
        )}
      </div>
    </>
  );
}

function ModerationImages({postId}:{postId:string}) {
  const [images,setImages] = useState<Media[]>([]);

  useEffect(() => {
    api.getImages(postId)
      .then(setImages)
      .catch(() => {});
  }, [postId]);

  if (!images.length) return null;

  return (
    <div className="media-grid">
      {images.map(image => (
        <img
          key={image.id}
          src={image.url}
          alt={image.originalName}
        />
      ))}
    </div>
  );
}

function NotificationsView({userId,tick,onError,onRefresh}:{userId:string;tick:number;onError:(x:string)=>void;onRefresh:()=>void}) { const [items,setItems]=useState<Notification[]>([]);const[unread,setUnread]=useState(false);useEffect(()=>{api.notifications(userId,unread).then(setItems).catch(e=>onError(e.message))},[userId,unread,tick]);return <><PageTitle eyebrow="ВХОДЯЩИЕ" title="Уведомления" text="Все события от notification-svc собраны здесь."/><div className="filter-row"><button className={!unread?"active-filter":""} onClick={()=>setUnread(false)}>Все</button><button className={unread?"active-filter":""} onClick={()=>setUnread(true)}>Непрочитанные</button></div><div className="notifications">{items.map(n=><div className={`notification ${n.isRead?"read":""}`} key={n.id}><div className="notification-icon"><Bell size={16}/></div><div className="notification-body"><strong>{notificationRu(n.type)}</strong><span>{n.postTitle||"Новое событие"}</span><small>{timeAgo(n.createdAt)}</small></div>{!n.isRead&&<button onClick={async()=>{try{await api.markNotificationRead(n.id);onRefresh()}catch(e){onError(e instanceof Error?e.message:"Не удалось отметить уведомление")}}}><Check size={15}/></button>}</div>)}{!items.length&&<Empty title="Здесь пусто" text="Новых уведомлений нет."/>}</div></> }

function ProfileView({userId,username,tick,onError,onRefresh}:{userId:string;username:string;tick:number;onError:(x:string)=>void;onRefresh:()=>void}) { const [user,setUser]=useState<UserResponse|null>(null); const [bio,setBio]=useState(""); const [edit,setEdit]=useState(false); useEffect(()=>{api.user(userId).then(u=>{setUser(u);setBio(u.bio||"")}).catch(()=>{})},[userId,tick]); const save=async()=>{try{const u=await api.updateBio(userId,bio);setUser(u);setEdit(false)}catch(e){onError(e instanceof Error?e.message:"Профиль недоступен через текущий Gateway")}};return <div className="profile-card"><div className="profile-cover"/><div className="profile-body"><div className="profile-avatar">{initials(username)}</div><div className="profile-head"><div><h1>{user?.username||username}</h1><small>{user?.email||""}</small></div><button className="ghost-button" onClick={()=>setEdit(v=>!v)}><Edit3 size={15}/> Изменить био</button></div>{edit?<div className="edit-panel"><textarea value={bio} onChange={e=>setBio(e.target.value)} placeholder="Расскажите о себе"/><button className="accent-button" onClick={save}>Сохранить</button></div>:<p className="bio">{user?.bio||"Добавьте короткое описание о себе."}</p>}<div className="profile-meta"><span>Аккаунт создан: <b>{user?.createdAt?new Date(user.createdAt).toLocaleDateString("ru-RU"):"—"}</b></span></div></div></div> }

function CreatePostModal({onClose,onCreated,onError}:{onClose:()=>void;onCreated:()=>void;onError:(x:string)=>void}) {
  const [channels,setChannels]=useState<Channel[]>([]);
  const [channelId,setChannelId]=useState("");
  const [title,setTitle]=useState("");
  const [content,setContent]=useState("");
  const [image,setImage]=useState<File|null>(null);
  const [busy,setBusy]=useState(false);
  const [loading,setLoading]=useState(true);

  useEffect(()=>{
    setLoading(true);
    loadKnownChannels(onError)
      .then(cs=>{
        setChannels(cs);
        setChannelId(current=>current || cs[0]?.id || "");
      })
      .finally(()=>setLoading(false));
  },[]);

  const create=async(e:FormEvent)=>{
    e.preventDefault();

    if(!channelId){
      onError("У вас пока нет доступных каналов. Сначала создайте канал.");
      return;
    }

    if(!title.trim() || !content.trim()){
      onError("Заполните заголовок и текст поста.");
      return;
    }

    setBusy(true);

    try{
      // 1. Создаём пост.
      const post=await api.createPost({
        channelId,
        title:title.trim(),
        content:content.trim()
      });

      rememberChannel(channelId);

      // 2. Если пользователь выбрал изображение — загружаем его к созданному посту.
      //    Делаем это до публикации, чтобы опубликованный пост сразу был полностью готов.
      if(image){
        await api.uploadImage(post.id,image);
      }

      // 3. Отправляем созданный пост на модерацию.
      //    Backend должен вернуть пост уже со статусом PENDING_MODERATION.
      const pendingPost = await api.moderatePost(post.id);

      // Сохраняем его локально, чтобы вкладка "Модерация" увидела
      // пост сразу, даже если endpoint списка канала возвращает
      // только PUBLISHED посты.
      rememberPendingModerationPost({
        ...pendingPost,
        status: "PENDING_MODERATION"
      });

      onCreated();
    }catch(err){
      onError(err instanceof Error?err.message:"Не удалось создать и отправить пост на модерацию");
    }finally{
      setBusy(false);
    }
  };

  return <Modal close={onClose}>
    <div className="modal-head">
      <div>
        <small>ПУБЛИКАЦИЯ</small>
        <h2>Создать пост</h2>
      </div>
      <button onClick={onClose}><X/></button>
    </div>

    <form onSubmit={create}>
      <label>
        Канал
        <select
          value={channelId}
          onChange={e=>setChannelId(e.target.value)}
          disabled={loading||!channels.length||busy}
          required
        >
          {!channels.length&&
            <option value="">
              {loading?"Загружаем каналы…":"Нет доступных каналов"}
            </option>
          }
          {channels.map(c=>
            <option key={c.id} value={c.id}>{c.name}</option>
          )}
        </select>
      </label>

      <label>
        Заголовок
        <input
          required
          value={title}
          onChange={e=>setTitle(e.target.value)}
          placeholder="О чём пост?"
          disabled={busy}
        />
      </label>

      <label>
        Текст
        <textarea
          required
          rows={7}
          value={content}
          onChange={e=>setContent(e.target.value)}
          placeholder="Поделитесь своей идеей..."
          disabled={busy}
        />
      </label>

      <label className="upload-button">
        <ImagePlus size={16}/>
        {image ? image.name : "Добавить фотографию"}
        <input
          type="file"
          accept="image/jpeg,image/png,image/webp,image/gif"
          disabled={busy}
          onChange={e=>setImage(e.target.files?.[0] || null)}
          hidden
        />
      </label>

      {image&&
        <div className="selected-file">
          <span>{image.name}</span>
          <button
            type="button"
            onClick={()=>setImage(null)}
            disabled={busy}
          >
            <X size={14}/>
          </button>
        </div>
      }

      <button
        className="accent-button full"
        disabled={busy||loading||!channelId}
      >
        {busy?"Отправляем на модерацию…":"Отправить на модерацию"}
        <Send size={16}/>
      </button>
    </form>
  </Modal>;
}

function AuthModal({mode,setMode,onClose,onSuccess,onError}:{mode:"login"|"register";setMode:(m:"login"|"register")=>void;onClose:()=>void;onSuccess:()=>void;onError:(x:string)=>void}) { const[username,setUsername]=useState("");const[email,setEmail]=useState("");const[password,setPassword]=useState("");const[busy,setBusy]=useState(false);const submit=async(e:FormEvent)=>{e.preventDefault();setBusy(true);try{const tokens=mode==="login"?await api.login({username,password}):await api.register({email,username,password});session.save(tokens);onSuccess()}catch(err){onError(err instanceof Error?err.message:"Не удалось войти")}finally{setBusy(false)}};return <Modal close={onClose}><div className="auth-brand"><span className="brand-mark"><Sparkles size={17}/></span><span>Postly</span></div><h2>{mode==="login"?"С возвращением":"Создание аккаунта"}</h2><p className="modal-subtitle">После входа сессия сохраняется в браузере — переход на «Главная» больше не требует повторного ввода.</p><form onSubmit={submit}><label>Имя пользователя<input required value={username} onChange={e=>setUsername(e.target.value)} placeholder="username"/></label>{mode==="register"&&<label>Email<input required type="email" value={email} onChange={e=>setEmail(e.target.value)} placeholder="you@example.com"/></label>}<label>Пароль<input required type="password" value={password} onChange={e=>setPassword(e.target.value)} placeholder="••••••••"/></label><button className="accent-button full" disabled={busy}>{busy?"Проверяем…":mode==="login"?"Войти":"Зарегистрироваться"}<LogIn size={16}/></button></form><button className="switch-auth" onClick={()=>setMode(mode==="login"?"register":"login")}>{mode==="login"?"Нет аккаунта? Зарегистрироваться":"Уже есть аккаунт? Войти"}</button></Modal>; }

function ChannelModal({title,onClose,onSave}:{title:string;onClose:()=>void;onSave:(name:string,description:string)=>void}) {const[n,setN]=useState("");const[d,setD]=useState("");return <Modal close={onClose}><div className="modal-head"><div><small>КАНАЛ</small><h2>{title}</h2></div><button onClick={onClose}><X/></button></div><label>Название<input value={n} onChange={e=>setN(e.target.value)} placeholder="Java"/></label><label>Описание<textarea value={d} onChange={e=>setD(e.target.value)} placeholder="О чём этот канал?"/></label><button className="accent-button full" onClick={()=>onSave(n,d)}>Сохранить</button></Modal>}

function UnreadBadge({userId,tick}:{userId:string;tick:number}){const[count,setCount]=useState(0);useEffect(()=>{api.unreadCount(userId).then(setCount).catch(()=>{})},[userId,tick]);return count>0?<span className="badge">{count>99?"99+":count}</span>:null}
function NavItem({icon,label,active,onClick}:{icon:any;label:string;active?:boolean;onClick:()=>void}){return <button className={`nav-item ${active?"active":""}`} onClick={onClick}>{icon}<span>{label}</span></button>}
function PageTitle({eyebrow,title,text}:{eyebrow:string;title:string;text:string}){return <section className="page-title"><small>{eyebrow}</small><h1>{title}</h1><p>{text}</p></section>}
function Modal({children,close}:{children:any;close:()=>void}){return <div className="modal-backdrop" onMouseDown={e=>e.target===e.currentTarget&&close()}><div className="modal">{children}</div></div>}
function Empty({title,text}:{title:string;text:string}){return <div className="empty"><div className="empty-icon"><Sparkles/></div><h2>{title}</h2><p>{text}</p></div>}
const PENDING_MODERATION_STORAGE_KEY = "postly.pendingModerationPosts";

function getPendingModerationPosts(): Post[] {
  try {
    const raw = localStorage.getItem(PENDING_MODERATION_STORAGE_KEY);
    if (!raw) return [];

    const parsed = JSON.parse(raw);

    if (!Array.isArray(parsed)) return [];

    return parsed.filter(
      (post): post is Post =>
        post &&
        typeof post.id === "string" &&
        normalizePostStatus(post.status) === "PENDING_MODERATION"
    );
  } catch {
    return [];
  }
}

function rememberPendingModerationPost(post: Post) {
  const current = getPendingModerationPosts()
    .filter(existing => existing.id !== post.id);

  current.push({
    ...post,
    status: "PENDING_MODERATION"
  });

  localStorage.setItem(
    PENDING_MODERATION_STORAGE_KEY,
    JSON.stringify(current)
  );
}

function forgetPendingModerationPost(postId: string) {
  const current = getPendingModerationPosts()
    .filter(post => post.id !== postId);

  localStorage.setItem(
    PENDING_MODERATION_STORAGE_KEY,
    JSON.stringify(current)
  );
}

async function loadKnownChannels(onError:(x:string)=>void):Promise<Channel[]> { const ids=new Set(getKnownChannelIds()); try{const subs=await api.mySubscriptions();subs.forEach(s=>ids.add(s.channelId))}catch{} const cs=await Promise.all([...ids].map(id=>api.getChannel(id).catch(()=>null))); return cs.filter(Boolean) as Channel[]; }
function initials(value?:string){return (value||"П").trim().slice(0,1).toUpperCase()}
function timeAgo(v:string){const s=Math.max(1,Math.floor((Date.now()-new Date(v).getTime())/1000));if(s<60)return `${s}с`;if(s<3600)return `${Math.floor(s/60)}м`;if(s<86400)return `${Math.floor(s/3600)}ч`;return `${Math.floor(s/86400)}д`}
function normalizePostStatus(v:string) {
  if (v === "MODERATION") return "PENDING_MODERATION";
  return v;
}

function statusRu(v:string){
  return ({
    DRAFT:"Черновик",
    PENDING_MODERATION:"На модерации",
    MODERATION:"На модерации",
    PUBLISHED:"Опубликован",
    HIDDEN:"Скрыт",
    REJECTED:"Отклонён",
    DELETED:"Удалён"
  } as Record<string,string>)[v] || v;
}
function notificationRu(v:string){return ({LIKE:"Новый лайк",SUBSCRIPTION:"Новая подписка",POST_PUBLISHED:"Новый пост"} as Record<string,string>)[v]||v}