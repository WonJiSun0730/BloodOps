import { useEffect, useState } from 'react'
import AdminSectionNav from '../../components/admin/AdminSectionNav.jsx'
import { createAdminRewardProduct, getAdminRewardProducts, updateAdminRewardProduct } from '../../api/apiClient.js'
import './AdminRewardProductsPage.css'

const emptyForm = { name: '', description: '', pointPrice: '', stockQuantity: '', active: true }

function AdminRewardProductsPage() {
  const [products, setProducts] = useState([])
  const [form, setForm] = useState(emptyForm)
  const [editingId, setEditingId] = useState(null)
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')

  const loadProducts = () => getAdminRewardProducts().then(setProducts).catch((e) => setError(e.message))
  useEffect(() => { loadProducts() }, [])

  const submit = async (event) => {
    event.preventDefault(); setError(''); setMessage('')
    const payload = { ...form, pointPrice: Number(form.pointPrice), stockQuantity: Number(form.stockQuantity) }
    try {
      if (editingId) await updateAdminRewardProduct(editingId, payload)
      else await createAdminRewardProduct(payload)
      setMessage(editingId ? '상품이 수정되었습니다.' : '상품이 등록되었습니다.')
      setEditingId(null); setForm(emptyForm); await loadProducts()
    } catch (requestError) { setError(requestError.message) }
  }

  const edit = (product) => {
    setEditingId(product.id)
    setForm({ name: product.name, description: product.description, pointPrice: product.pointPrice, stockQuantity: product.stockQuantity, active: product.active })
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }

  return (
    <main className="admin-reward-page">
      <AdminSectionNav />
      <header className="admin-reward-header"><span>REWARD INVENTORY</span><h1>포인트 상품·재고 관리</h1><p>헌혈자가 포인트로 선택할 상품과 재고를 관리합니다.</p></header>
      <form className="admin-reward-form" onSubmit={submit}>
        <label>상품명<input required maxLength="100" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} /></label>
        <label>필요 포인트<input required type="number" min="1" value={form.pointPrice} onChange={(e) => setForm({ ...form, pointPrice: e.target.value })} /></label>
        <label>재고 수량<input required type="number" min="0" value={form.stockQuantity} onChange={(e) => setForm({ ...form, stockQuantity: e.target.value })} /></label>
        <label className="admin-reward-description">상품 설명<textarea required maxLength="500" value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} /></label>
        <label className="admin-reward-check"><input type="checkbox" checked={form.active} onChange={(e) => setForm({ ...form, active: e.target.checked })} /> 사용자에게 상품 노출</label>
        <div className="admin-reward-actions"><button type="submit">{editingId ? '수정 저장' : '상품 등록'}</button>{editingId && <button type="button" className="secondary" onClick={() => { setEditingId(null); setForm(emptyForm) }}>취소</button>}</div>
      </form>
      {message && <p className="admin-reward-message success">{message}</p>}
      {error && <p className="admin-reward-message error">{error}</p>}
      <section className="admin-reward-list">
        <header><h2>등록 상품</h2><span>{products.length}개</span></header>
        <div className="admin-reward-table">
          <div className="admin-reward-row heading"><span>상품명</span><span>필요 포인트</span><span>재고</span><span>상태</span><span>관리</span></div>
          {products.map((product) => <div className="admin-reward-row" key={product.id}><span><strong>{product.name}</strong><small>{product.description}</small></span><span>{product.pointPrice.toLocaleString()}P</span><span>{product.stockQuantity}개</span><span>{product.active ? '노출' : '숨김'}</span><span><button type="button" onClick={() => edit(product)}>수정</button></span></div>)}
        </div>
      </section>
    </main>
  )
}

export default AdminRewardProductsPage
