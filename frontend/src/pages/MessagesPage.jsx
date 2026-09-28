import { useEffect, useRef, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { messageService } from '../services/domainServices'
import { useAuth } from '../context/AuthContext.jsx'
import { EmptyState, Spinner } from '../components/Feedback.jsx'
import ScheduleSessionModal from '../components/ScheduleSessionModal.jsx'

export default function MessagesPage() {
  const { user } = useAuth()
  const { conversationId } = useParams()
  const navigate = useNavigate()
  const [conversations, setConversations] = useState(null)
  const [messages, setMessages] = useState([])
  const [text, setText] = useState('')
  const [showSchedule, setShowSchedule] = useState(false)
  const bottomRef = useRef(null)

  const activeConversation = conversations?.find((c) => String(c.id) === conversationId)

  function loadConversations() {
    messageService.listConversations().then(setConversations)
  }

  useEffect(loadConversations, [])

  useEffect(() => {
    if (!conversationId) return
    let mounted = true
    function poll() {
      messageService.getMessages(conversationId).then((m) => mounted && setMessages(m))
    }
    poll()
    const interval = setInterval(poll, 4000)
    return () => { mounted = false; clearInterval(interval) }
  }, [conversationId])

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages])

  async function handleSend(e) {
    e.preventDefault()
    if (!text.trim()) return
    const sent = await messageService.send(conversationId, text.trim())
    setMessages((prev) => [...prev, sent])
    setText('')
    loadConversations()
  }

  if (conversations === null) return <Spinner />

  return (
    <div className="grid h-[calc(100vh-140px)] grid-cols-1 gap-4 md:grid-cols-3">
      <div className="overflow-y-auto rounded-xl2 border border-slate-100 bg-white dark:border-slate-800 dark:bg-slate-900">
        {conversations.length === 0 ? (
          <div className="p-4"><EmptyState title="No conversations yet" description="Accept a swap request to start chatting." /></div>
        ) : (
          conversations.map((c) => (
            <button
              key={c.id}
              onClick={() => navigate(`/messages/${c.id}`)}
              className={`flex w-full items-center gap-3 border-b border-slate-50 px-4 py-3 text-left hover:bg-slate-50 dark:border-slate-800 dark:hover:bg-slate-800 ${
                String(c.id) === conversationId ? 'bg-brand-50 dark:bg-brand-900/30' : ''
              }`}
            >
              <span className="flex h-9 w-9 shrink-0 items-center justify-center rounded-full bg-brand-100 text-sm font-semibold text-brand-700">
                {c.otherUserName[0]}
              </span>
              <div className="min-w-0 flex-1">
                <p className="truncate text-sm font-semibold text-slate-800 dark:text-slate-100">{c.otherUserName}</p>
                <p className="truncate text-xs text-slate-400">{c.lastMessage || 'Say hello!'}</p>
              </div>
              {c.unreadCount > 0 && (
                <span className="flex h-5 min-w-5 items-center justify-center rounded-full bg-brand-600 px-1.5 text-[10px] font-bold text-white">
                  {c.unreadCount}
                </span>
              )}
            </button>
          ))
        )}
      </div>

      <div className="flex flex-col rounded-xl2 border border-slate-100 bg-white dark:border-slate-800 dark:bg-slate-900 md:col-span-2">
        {!conversationId ? (
          <div className="flex h-full items-center justify-center text-sm text-slate-400">Select a conversation</div>
        ) : (
          <>
            <div className="flex items-center justify-between border-b border-slate-100 p-3 dark:border-slate-800">
              <p className="text-sm font-semibold text-slate-800 dark:text-slate-100">{activeConversation?.otherUserName}</p>
              <button onClick={() => setShowSchedule(true)} className="btn-secondary text-xs">Schedule session</button>
            </div>
            <div className="flex-1 space-y-2 overflow-y-auto p-4">
              {messages.length === 0 ? (
                <EmptyState title="No messages yet" description="Send the first message below." />
              ) : (
                messages.map((m) => (
                  <div key={m.id} className={`flex ${m.senderId === user.id ? 'justify-end' : 'justify-start'}`}>
                    <div className={`max-w-xs rounded-xl px-3.5 py-2 text-sm ${
                      m.senderId === user.id ? 'bg-brand-600 text-white' : 'bg-slate-100 text-slate-800 dark:bg-slate-800 dark:text-slate-100'
                    }`}>
                      {m.content}
                      <p className={`mt-1 text-[10px] ${m.senderId === user.id ? 'text-brand-100' : 'text-slate-400'}`}>
                        {new Date(m.sentAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                      </p>
                    </div>
                  </div>
                ))
              )}
              <div ref={bottomRef} />
            </div>
            <form onSubmit={handleSend} className="flex gap-2 border-t border-slate-100 p-3 dark:border-slate-800">
              <input className="input-field" placeholder="Type a message…" value={text} onChange={(e) => setText(e.target.value)} />
              <button className="btn-primary">Send</button>
            </form>
          </>
        )}
      </div>

      {showSchedule && activeConversation && (
        <ScheduleSessionModal
          otherUserId={activeConversation.otherUserId}
          onClose={() => setShowSchedule(false)}
          onScheduled={() => navigate('/sessions')}
        />
      )}
    </div>
  )
}
