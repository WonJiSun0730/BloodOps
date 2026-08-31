import { useEffect, useState } from 'react'
import AdminSectionNav from '../../components/admin/AdminSectionNav.jsx'
import { createAdminRewardProduct, deleteAdminRewardProduct, getAdminRewardProducts, updateAdminRewardProduct, uploadAdminRewardProductImage } from '../../api/apiClient.js'
import './AdminRewardProductsPage.css'

const emptyForm = { name: '', description: '', pointPrice: '', stockQuantity: '', productType: 'PHYSICAL', active: true }

function AdminRewardProductsPage() {
  const [products, setProducts] = useState([])
  const [form, setForm] = useState(emptyForm)
  const [editingId, setEditingId] = useState(null)
  const [imageFile, setImageFile] = useState(null)
  const [imagePreview, setImagePreview] = useState('')
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')

  const loadProducts = () => getAdminRewardProducts().then(setProducts).catch((e) => setError(e.message))
  useEffect(() => { loadProducts() }, [])

  const submit = async (event) => {
    event.preventDefault(); setError(''); setMessage('')
    const payload = { ...form, pointPrice: Number(form.pointPrice), stockQuantity: Number(form.stockQuantity) }
    try {
      const savedProduct = editingId
        ? await updateAdminRewardProduct(editingId, payload)
        : await createAdminRewardProduct(payload)
      if (imageFile) {
        await uploadAdminRewardProductImage(savedProduct.id, imageFile)
      }
      setMessage(editingId ? '상품이 수정되었습니다.' : '상품이 등록되었습니다.')
      setEditingId(null); setForm(emptyForm); setImageFile(null); setImagePreview(''); await loadProducts()
    } catch (requestError) { setError(requestError.message) }
  }

  const remove = async (product) => {
    if (!window.confirm('"' + product.name + '" 상품을 삭제하시겠습니까?\n삭제한 상품과 이미지는 복구할 수 없습니다.')) return
    setError(''); setMessage('')
    try {
      await deleteAdminRewardProduct(product.id)
      if (editingId === product.id) {
        setEditingId(null); setForm(emptyForm); setImageFile(null); setImagePreview('')
      }
      setMessage('상품이 삭제되었습니다.')
      await loadProducts()
    } catch (requestError) {
      setError(requestError.message)
    }
  }

  const edit = (product) => {
    setEditingId(product.id)
    setForm({ name: product.name, description: product.description, pointPrice: product.pointPrice, stockQuantity: product.stockQuantity, productType: product.productType || 'PHYSICAL', active: product.active })
    setImageFile(null)
    setImagePreview(product.imageUrl || '')
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }

  return (
    <main className="admin-reward-page">
      <AdminSectionNav />
      <header className="admin-reward-header"><span>REWARD INVENTORY</span><h1>포인트 상품·재고 관리</h1><p>헌혈자가 포인트로 선택할 상품과 재고를 관리합니다.</p></header>
      <form className="admin-reward-form" onSubmit={submit}>
        <label>상품명<input required maxLength="100" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} /></label>
        <label>상품 유형<select value={form.productType} onChange={(e) => setForm({ ...form, productType: e.target.value, stockQuantity: e.target.value === 'DIGITAL_VOUCHER' ? 0 : form.stockQuantity })}><option value="PHYSICAL">실물 상품</option><option value="DIGITAL_VOUCHER">디지털 코드</option></select></label>
        <label>필요 포인트<input required type="number" min="1" value={form.pointPrice} onChange={(e) => setForm({ ...form, pointPrice: e.target.value })} /></label>
        <label>재고 수량<input required type="number" min="0" disabled={form.productType === 'DIGITAL_VOUCHER'} value={form.stockQuantity} onChange={(e) => setForm({ ...form, stockQuantity: e.target.value })} /></label>
        <label className="admin-reward-image">상품 이미지
          <input type="file" accept="image/jpeg,image/png,image/webp,image/gif" onChange={(e) => {
            const file = e.target.files?.[0] || null
            setImageFile(file)
            setImagePreview(file ? URL.createObjectURL(file) : '')
          }} />
          <small>JPG, PNG, WebP 등 이미지 파일 · 최대 5MB</small>
        </label>
        {imagePreview && <img className="admin-reward-preview" src={imagePreview} alt="상품 이미지 미리보기" />}
        <label className="admin-reward-description">상품 설명<textarea required maxLength="500" value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} /></label>
        <label className="admin-reward-check"><input type="checkbox" checked={form.active} onChange={(e) => setForm({ ...form, active: e.target.checked })} /> 사용자에게 상품 노출</label>
        <div className="admin-reward-actions"><button type="submit">{editingId ? '수정 저장' : '상품 등록'}</button>{editingId && <button type="button" className="secondary" onClick={() => { setEditingId(null); setForm(emptyForm); setImageFile(null); setImagePreview('') }}>취소</button>}</div>
      </form>
      {message && <p className="admin-reward-message success">{message}</p>}
      {error && <p className="admin-reward-message error">{error}</p>}
      <section className="admin-reward-list">
        <header><h2>등록 상품</h2><span>{products.length}개</span></header>
        <div className="admin-reward-table">
          <div className="admin-reward-row heading"><span>상품명</span><span>필요 포인트</span><span>재고</span><span>상태</span><span>관리</span></div>
          {products.map((product) => <div className="admin-reward-row" key={product.id}><span className="admin-reward-product-name">{product.imageUrl ? <img src={product.imageUrl} alt="" /> : <span className="admin-reward-no-image">NO IMAGE</span>}<span><strong>{product.name}</strong><small>{product.description}</small></span></span><span>{product.pointPrice.toLocaleString()}P</span><span>{product.productType === 'DIGITAL_VOUCHER' ? '디지털 코드 · ' : '실물 · '}{product.stockQuantity}개</span><span>{product.active ? '노출' : '숨김'}</span><span className="admin-reward-row-actions"><button type="button" onClick={() => edit(product)}>수정</button><button type="button" className="danger" onClick={() => remove(product)}>삭제</button></span></div>)}
        </div>
      </section>
    </main>
  )
}

export default AdminRewardProductsPage
